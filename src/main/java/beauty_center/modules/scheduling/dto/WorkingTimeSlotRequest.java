package beauty_center.modules.scheduling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * Working time slot request DTO.
 * Used to create or update employee working hours.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkingTimeSlotRequest {

    @NotBlank(message = "Day of week is required")
    @Pattern(regexp = "MON|TUE|WED|THU|FRI|SAT|SUN", message = "Invalid day of week")
    private String dayOfWeek;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

}

