package beauty_center.modules.appointments.service;

import beauty_center.modules.appointments.entity.Appointment;
import beauty_center.modules.appointments.repository.AppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;

/**
 * Appointment service managing booking operations.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    /**
     * Get appointment by ID
     */
    public Optional<Appointment> getAppointmentById(UUID id) {
        return appointmentRepository.findById(id);
    }

    /**
     * Create new appointment with availability validation
     */
    public Appointment createAppointment(Appointment appointment) {
        // TODO: Validate employee availability
        // TODO: Check service duration fits in slot
        // TODO: Validate client exists
        // TODO: Set default status to PENDING
        return appointmentRepository.save(appointment);
    }

    /**
     * Update appointment
     */
    public Appointment updateAppointment(UUID id, Appointment updates) {
        // TODO: Validate if rescheduling is allowed
        // TODO: Check new availability
        return appointmentRepository.save(updates);
    }

    /**
     * Cancel appointment
     */
    public void cancelAppointment(UUID id, String reason) {
        appointmentRepository.findById(id).ifPresent(appointment -> {
            // TODO: Set status to CANCELED
            // TODO: Store cancellation reason
            // TODO: Trigger notification
            appointmentRepository.save(appointment);
        });
    }

}
