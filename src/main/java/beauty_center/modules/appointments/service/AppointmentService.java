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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Appointment service for querying and managing appointments.
 * For creating appointments, use BookingService.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final BeautyServiceRepository beautyServiceRepository;
    private final BeautyServiceEmployeeRepository beautyServiceEmployeeRepository;
    private final UserAccountRepository userAccountRepository;
    private final AvailabilityService availabilityService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    /**
     * Get appointment by ID
     */
    @Transactional(readOnly = true)
    public Optional<Appointment> getAppointmentById(UUID id) {
        return appointmentRepository.findById(id);
    }

    /**
     * Get appointments with filters
     */
    @Transactional(readOnly = true)
    public List<Appointment> getAppointments(
            UUID clientId,
            UUID employeeId,
            AppointmentStatus status,
            OffsetDateTime startAt,
            OffsetDateTime endAt) {

        return appointmentRepository.findWithFilters(clientId, employeeId, status, startAt, endAt);
    }

    /**
     * Get appointments for a specific client
     */
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByClient(UUID clientId) {
        return appointmentRepository.findByClientId(clientId);
    }

    /**
     * Get appointments for a specific employee
     */
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByEmployee(UUID employeeId) {
        return appointmentRepository.findByEmployeeId(employeeId);
    }

    /**
     * Update/reschedule an appointment.
     * Only allowed for CONFIRMED appointments.
     *
     * @param id Appointment ID
     * @param employeeId New employee ID
     * @param serviceId New service ID
     * @param startAt New start time
     * @return Updated appointment
     * @throws IllegalArgumentException if validation fails
     * @throws IllegalStateException if appointment cannot be updated
     */
    public Appointment updateAppointment(
            UUID id,
            UUID employeeId,
            UUID serviceId,
            OffsetDateTime startAt) {

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + id));

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Cannot update appointment with status: " + appointment.getStatus());
        }

        BeautyService service = beautyServiceRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Service not found: " + serviceId));

        if (!service.isActive()) {
            throw new IllegalArgumentException("Service is not active: " + serviceId);
        }

        if (!beautyServiceEmployeeRepository.existsByBeautyServiceIdAndEmployeeId(serviceId, employeeId)) {
            log.warn("Employee {} is not allowed to perform service {}", employeeId, serviceId);
            throw new IllegalArgumentException(
                    "Employee is not authorized to perform this service: " + serviceId);
        }

        OffsetDateTime endAt = startAt.plusMinutes(service.getDurationMin());

        boolean hasOverlap = appointmentRepository.existsOverlappingAppointment(
                employeeId, startAt, endAt, id);

        if (hasOverlap) {
            throw new BookingService.AppointmentConflictException(
                    "The requested time slot is not available");
        }

        if (!availabilityService.isAvailable(employeeId, startAt, endAt)) {
            log.warn("Time slot not available for employee {} from {} to {} - outside working hours or during absence",
                    employeeId, startAt, endAt);
            throw new BookingService.AppointmentConflictException(
                    "The requested time slot is not available. Please choose another time.");
        }

        appointment.setEmployeeId(employeeId);
        appointment.setBeautyServiceId(serviceId);
        appointment.setStartAt(startAt);
        appointment.setEndAt(endAt);

        Appointment updated = appointmentRepository.save(appointment);
        log.info("Appointment {} rescheduled to {}", id, startAt);

        try { auditService.logUpdate("Appointment", id, null, updated); } catch (Exception e) { log.error("Audit log failed: {}", e.getMessage()); }

        return updated;
    }

    /**
     * Reassign an appointment to a different employee.
     * Only ADMIN can reassign appointments.
     * Validates that new employee is eligible and available at the appointment time.
     *
     * @param id Appointment ID
     * @param newEmployeeId New employee UUID
     * @return Updated appointment
     * @throws IllegalArgumentException if appointment/employee not found or validation fails
     * @throws IllegalStateException if appointment is not CONFIRMED
     * @throws BookingService.AppointmentConflictException if new employee not available
     */
    public Appointment reassignAppointment(UUID id, UUID newEmployeeId) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + id));

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Cannot reassign appointment with status: " + appointment.getStatus());
        }

        // Validate new employee exists
        if (!userAccountRepository.existsById(newEmployeeId)) {
            throw new IllegalArgumentException("Employee not found: " + newEmployeeId);
        }

        // Check if new employee is allowed to perform the service
        if (!beautyServiceEmployeeRepository.existsByBeautyServiceIdAndEmployeeId(
                appointment.getBeautyServiceId(), newEmployeeId)) {
            log.warn("Employee {} is not authorized to perform service {}",
                    newEmployeeId, appointment.getBeautyServiceId());
            throw new IllegalArgumentException(
                    "Employee is not authorized to perform this service");
        }

        // Check if new employee is available at the appointment time
        boolean hasOverlap = appointmentRepository.existsOverlappingAppointment(
                newEmployeeId, appointment.getStartAt(), appointment.getEndAt(), id);

        if (hasOverlap) {
            log.warn("New employee {} has conflicting appointments at the requested time", newEmployeeId);
            throw new BookingService.AppointmentConflictException(
                    "The selected employee is not available at this time");
        }

        // Check working hours and absences
        if (!availabilityService.isAvailable(newEmployeeId, appointment.getStartAt(), appointment.getEndAt())) {
            log.warn("New employee {} is not available (working hours or absence) at the requested time", newEmployeeId);
            throw new BookingService.AppointmentConflictException(
                    "The selected employee is not available at this time");
        }

        UUID oldEmployeeId = appointment.getEmployeeId();
        appointment.setEmployeeId(newEmployeeId);
        Appointment updated = appointmentRepository.save(appointment);
        log.info("Appointment {} reassigned from employee {} to {}", id, oldEmployeeId, newEmployeeId);

        try { auditService.logUpdate("Appointment", id, null, updated); } catch (Exception e) { log.error("Audit log failed: {}", e.getMessage()); }

        return updated;
    }

    /**
     * Cancel an appointment.
     * Sets status to CANCELED and stores cancellation reason.
     *
     * @param id Appointment ID
     * @param reason Cancellation reason
     * @throws IllegalArgumentException if appointment not found
     * @throws IllegalStateException if appointment already canceled or completed
     */
    public void cancelAppointment(UUID id, String reason) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + id));

        if (appointment.getStatus() == AppointmentStatus.CANCELED) {
            throw new IllegalStateException("Appointment is already canceled");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed appointment");
        }

        appointment.setStatus(AppointmentStatus.CANCELED);
        appointment.setCancellationReason(reason);

        appointmentRepository.save(appointment);
        log.info("Appointment {} canceled. Reason: {}", id, reason);

        try { auditService.logUpdate("Appointment", id, null, appointment); } catch (Exception e) { log.error("Audit log failed: {}", e.getMessage()); }

        // Cancel any scheduled notifications
        try {
            notificationService.cancelNotificationsForAppointment(id);
        } catch (Exception e) {
            log.error("Failed to cancel notifications for appointment {}: {}", id, e.getMessage());
        }
    }

    /**
     * Mark appointment as completed.
     * Only allowed for CONFIRMED appointments.
     *
     * @param id Appointment ID
     * @throws IllegalArgumentException if appointment not found
     * @throws IllegalStateException if appointment not confirmed
     */
    public void completeAppointment(UUID id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found: " + id));

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Only CONFIRMED appointments can be marked as COMPLETED");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);
        log.info("Appointment {} marked as completed", id);

        try { auditService.logUpdate("Appointment", id, null, appointment); } catch (Exception e) { log.error("Audit log failed: {}", e.getMessage()); }
    }

}
