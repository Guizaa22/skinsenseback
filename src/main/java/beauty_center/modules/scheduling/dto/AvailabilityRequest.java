package beauty_center.modules.scheduling.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO for checking employee availability.
 * Used to query available time slots for booking.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityRequest {

    @NotNull(message = "Employee ID is required")
    private UUID employeeId;

    @NotNull(message = "Service ID is required")
    private UUID serviceId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @Min(value = 1, message = "Days must be at least 1")
    private Integer days = 1; // Default to 1 day

}

