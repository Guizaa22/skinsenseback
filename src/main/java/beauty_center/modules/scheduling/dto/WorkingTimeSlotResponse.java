package beauty_center.modules.scheduling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Working time slot response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkingTimeSlotResponse {

    private UUID id;
    private UUID employeeId;
    private String dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

}

