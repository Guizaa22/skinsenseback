package beauty_center.modules.scheduling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO containing available time slots for an employee.
 * Returns list of available slots within the requested date range.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityResponse {

    private UUID employeeId;
    private UUID serviceId;
    private LocalDate startDate;
    private LocalDate endDate;
    private int serviceDurationMinutes;
    private List<TimeSlot> availableSlots;

}

