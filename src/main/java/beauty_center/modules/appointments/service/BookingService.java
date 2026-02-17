package beauty_center.modules.appointments.service;

import beauty_center.modules.appointments.entity.Appointment;
import beauty_center.modules.appointments.entity.AppointmentStatus;
import beauty_center.modules.appointments.repository.AppointmentRepository;
import beauty_center.modules.audit.service.AuditService;
import beauty_center.modules.notifications.service.NotificationService;
import beauty_center.modules.scheduling.service.AvailabilityService;
import beauty_center.modules.services.entity.BeautyService;
import beauty_center.modules.services.repository.BeautyServiceEmployeeRepository;
import beauty_center.modules.services.repository.BeautyServiceRepository;
import beauty_center.modules.users.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Booking service for creating and managing appointments.
 * Handles transactional booking with availability validation and conflict detection.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BookingService {

    private final AppointmentRepository appointmentRepository;
    private final BeautyServiceRepository beautyServiceRepository;
    private final BeautyServiceEmployeeRepository beautyServiceEmployeeRepository;
    private final UserAccountRepository userAccountRepository;
    private final AvailabilityService availabilityService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    // Locks per employee to prevent concurrent bookings for the same employee
    private final ConcurrentHashMap<UUID, Lock> employeeLocks = new ConcurrentHashMap<>();

    /**
     * Create a new appointment with availability validation.
     * 
     * @param clientId Client UUID
     * @param employeeId Employee UUID
     * @param serviceId Service UUID
     * @param startAt Appointment start time
     * @param notes Optional notes
     * @return Created appointment
     * @throws IllegalArgumentException if validation fails
     * @throws AppointmentConflictException if time slot is not available (409)
     */
    public Appointment createAppointment(
            UUID clientId,
            UUID employeeId,
            UUID serviceId,
            OffsetDateTime startAt,
            String notes) {

        log.info("Creating appointment for client {} with employee {} at {}",
                clientId, employeeId, startAt);

        // Get or create a lock for this employee to serialize concurrent bookings
        Lock employeeLock = employeeLocks.computeIfAbsent(employeeId, k -> new ReentrantLock());

        employeeLock.lock();
        try {
            // Validate client exists
            if (!userAccountRepository.existsById(clientId)) {
                throw new IllegalArgumentException("Client not found: " + clientId);
            }

            // Validate employee exists
            if (!userAccountRepository.existsById(employeeId)) {
                throw new IllegalArgumentException("Employee not found: " + employeeId);
            }

            // Get service and validate
            BeautyService service = beautyServiceRepository.findById(serviceId)
                    .orElseThrow(() -> new IllegalArgumentException("Service not found: " + serviceId));

            if (!service.isActive()) {
                throw new IllegalArgumentException("Service is not active: " + serviceId);
            }

            // Check if employee is allowed to perform this service
            if (!beautyServiceEmployeeRepository.existsByBeautyServiceIdAndEmployeeId(serviceId, employeeId)) {
                log.warn("Employee {} is not allowed to perform service {}", employeeId, serviceId);
                throw new IllegalArgumentException(
                        "Employee is not authorized to perform this service: " + serviceId);
            }

            // Calculate end time based on service duration
            OffsetDateTime endAt = startAt.plusMinutes(service.getDurationMin());

            // Check for overlapping appointments
            List<Appointment> overlapping = appointmentRepository.findOverlappingAppointmentsWithLock(
                    employeeId, startAt, endAt);

            if (!overlapping.isEmpty()) {
                log.warn("Time slot not available for employee {} from {} to {} - found {} overlapping appointments",
                        employeeId, startAt, endAt, overlapping.size());
                throw new AppointmentConflictException(
                        "The requested time slot is not available. Please choose another time.");
            }

            // Double-check with availability service (checks working hours and absences)
            if (!availabilityService.isAvailable(employeeId, startAt, endAt)) {
                log.warn("Time slot not available for employee {} from {} to {} - outside working hours or during absence",
                        employeeId, startAt, endAt);
                throw new AppointmentConflictException(
                        "The requested time slot is not available. Please choose another time.");
            }

            // Create appointment
            Appointment appointment = Appointment.builder()
                    .id(UUID.randomUUID())
                    .clientId(clientId)
                    .employeeId(employeeId)
                    .beautyServiceId(serviceId)
                    .startAt(startAt)
                    .endAt(endAt)
                    .status(AppointmentStatus.CONFIRMED)
                    .build();

            try {
                Appointment saved = appointmentRepository.save(appointment);
                log.info("Appointment created successfully: {}", saved.getId());

                // Audit log
                try { auditService.logCreate("Appointment", saved.getId(), saved); } catch (Exception e) { log.error("Audit log failed: {}", e.getMessage()); }

                // Schedule notifications (booking confirmation + reminders)
                try {
                    notificationService.scheduleNotificationsForAppointment(saved);
                } catch (Exception e) {
                    log.error("Failed to schedule notifications for appointment {}: {}", saved.getId(), e.getMessage());
                    // Don't fail the booking if notifications fail
                }

                return saved;
            } catch (DataIntegrityViolationException e) {
                // Database exclusion constraint caught a race condition
                log.error("Appointment conflict detected by database constraint", e);
                throw new AppointmentConflictException(
                        "The requested time slot was just booked by another client. Please choose another time.");
            }
        } finally {
            employeeLock.unlock();
        }
    }

    /**
     * Custom exception for appointment conflicts (HTTP 409).
     */
    public static class AppointmentConflictException extends RuntimeException {
        public AppointmentConflictException(String message) {
            super(message);
        }
    }

}

