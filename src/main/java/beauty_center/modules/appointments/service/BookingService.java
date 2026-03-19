package beauty_center.modules.appointments.service;

import beauty_center.common.error.EntityNotFoundException;
import beauty_center.modules.appointments.entity.Appointment;
import beauty_center.modules.appointments.entity.AppointmentStatus;
import beauty_center.modules.appointments.repository.AppointmentRepository;
import beauty_center.modules.audit.service.AuditService;
import beauty_center.modules.notifications.service.NotificationService;
import beauty_center.modules.scheduling.service.AvailabilityService;
import beauty_center.modules.services.entity.BeautyService;
import beauty_center.modules.services.entity.BeautyServiceEmployee;
import beauty_center.modules.services.repository.BeautyServiceEmployeeRepository;
import beauty_center.modules.services.repository.BeautyServiceRepository;
import beauty_center.modules.users.entity.UserAccount;
import beauty_center.modules.users.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
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
 * Supports auto-assignment of eligible employees or explicit employee selection.
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
     * Create a new appointment with auto-assignment of eligible employee.
     * Used by FrontOffice booking without employee selection.
     *
     * @param clientId Client UUID
     * @param serviceId Service UUID
     * @param startAt Appointment start time
     * @param notes Optional notes
     * @return Created appointment with auto-assigned employee
     * @throws IllegalArgumentException if service/client not found or validation fails
     * @throws AppointmentConflictException if no eligible employee is available (409/422)
     */
    public Appointment createAppointmentWithAutoAssignment(
            UUID clientId,
            UUID serviceId,
            OffsetDateTime startAt,
            String notes) {

        log.info("Creating appointment for client {} with auto-assignment for service {} at {}",
                clientId, serviceId, startAt);

        // Validate client exists
        if (!userAccountRepository.existsById(clientId)) {
            throw new EntityNotFoundException("Client", clientId);
        }

        // Get service and validate
        BeautyService service = beautyServiceRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Service not found: " + serviceId));

        if (!service.isActive()) {
            throw new IllegalArgumentException("Service is not active: " + serviceId);
        }

        // Calculate end time based on service duration
        OffsetDateTime endAt = startAt.plusMinutes(service.getDurationMin());

        // Find all eligible employees for this service (active only)
        List<BeautyServiceEmployee> eligibleMappings = beautyServiceEmployeeRepository
                .findByBeautyServiceId(serviceId);

        if (eligibleMappings.isEmpty()) {
            log.warn("No employees assigned to service {}", serviceId);
            throw new AppointmentConflictException(
                    "No available employee for this service at the selected time.");
        }

        // Get active employees only and sort deterministically by UUID
        List<UUID> eligibleEmployeeIds = eligibleMappings.stream()
                .map(BeautyServiceEmployee::getEmployeeId)
                .distinct()
                .filter(empId -> {
                    UserAccount emp = userAccountRepository.findById(empId).orElse(null);
                    return emp != null && emp.isActive();
                })
                .sorted()
                .toList();

        if (eligibleEmployeeIds.isEmpty()) {
            log.warn("No active employees available for service {}", serviceId);
            throw new AppointmentConflictException(
                    "No available employee for this service at the selected time.");
        }

        // Find first available employee (deterministic: sorted by UUID)
        for (UUID employeeId : eligibleEmployeeIds) {
            if (isEmployeeAvailable(employeeId, startAt, endAt)) {
                return createAppointmentForSpecificEmployee(clientId, employeeId, serviceId, startAt, endAt, notes);
            }
        }

        log.warn("No eligible active employee is available for service {} from {} to {}",
                serviceId, startAt, endAt);
        throw new AppointmentConflictException(
                "No available employee for this service at the selected time.");
    }

    /**
     * Create a new appointment with explicit employee selection.
     * Used by admin or for backward compatibility.
     *
     * @param clientId Client UUID
     * @param employeeId Employee UUID
     * @param serviceId Service UUID
     * @param startAt Appointment start time
     * @param notes Optional notes (unused - notes are stored separately)
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
                throw new EntityNotFoundException("Client", clientId);
            }

            // Validate employee exists
            if (!userAccountRepository.existsById(employeeId)) {
                throw new EntityNotFoundException("Employee", employeeId);
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

            return createAppointmentForSpecificEmployee(clientId, employeeId, serviceId, startAt, endAt, notes);
        } finally {
            employeeLock.unlock();
        }
    }

    /**
     * Helper method to create and save an appointment for a specific employee.
     * Handles the actual persistence, audit, and notification logic.
     */
    private Appointment createAppointmentForSpecificEmployee(
            UUID clientId,
            UUID employeeId,
            UUID serviceId,
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            String notes) {

        // Create appointment
        Appointment appointment = Appointment.builder()
                .id(UUID.randomUUID())
                .clientId(clientId)
                .employeeId(employeeId)
                .beautyServiceId(serviceId)
                .startAt(startAt)
                .endAt(endAt)
                .status(AppointmentStatus.PENDING)
                .build();

        try {
            Appointment saved = appointmentRepository.save(appointment);
            log.info("Appointment created successfully: {}", saved.getId());

            // Log audit entry for new appointment
            log.info("New PENDING appointment created: {} for client: {} service: {}",
                    saved.getId(), saved.getClientId(), saved.getBeautyServiceId());

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
    }

    /**
     * Check if an employee is available at a specific time without locking.
     * Used internally during employee selection.
     *
     * @param employeeId Employee UUID
     * @param startAt Slot start time
     * @param endAt Slot end time
     * @return true if available, false otherwise
     */
    private boolean isEmployeeAvailable(UUID employeeId, OffsetDateTime startAt, OffsetDateTime endAt) {
        // Check for overlapping appointments
        List<Appointment> overlapping = appointmentRepository.findOverlappingAppointmentsWithLock(
                employeeId, startAt, endAt);

        if (!overlapping.isEmpty()) {
            log.debug("Employee {} has overlapping appointments from {} to {}", employeeId, startAt, endAt);
            return false;
        }

        // Check working hours and absences
        if (!availabilityService.isAvailable(employeeId, startAt, endAt)) {
            log.debug("Employee {} is not available (working hours or absence) from {} to {}", employeeId, startAt, endAt);
            return false;
        }

        return true;
    }

    /**
     * Custom exception for appointment conflicts (HTTP 409/422).
     */
    public static class AppointmentConflictException extends RuntimeException {
        public AppointmentConflictException(String message) {
            super(message);
        }
    }

}

