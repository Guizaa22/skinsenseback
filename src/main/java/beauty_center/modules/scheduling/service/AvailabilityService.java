package beauty_center.modules.scheduling.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Availability service calculating employee availability.
 * Core engine for appointment scheduling validation.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AvailabilityService {

    // TODO: Inject WorkingTimeSlotRepository
    // TODO: Inject AbsenceRepository
    // TODO: Inject AppointmentRepository

    /**
     * Check if employee is available at given time slot
     */
    public boolean isAvailable(UUID employeeId, LocalDateTime startAt, LocalDateTime endAt) {
        // TODO: Check working hours for day of week
        // TODO: Check no overlapping appointments
        // TODO: Check no absences during period
        return true;
    }

    /**
     * Get next available slot for employee
     */
    public LocalDateTime getNextAvailableSlot(UUID employeeId, LocalDateTime from, int durationMinutes) {
        // TODO: Find next free slot respecting duration
        return null;
    }

}
