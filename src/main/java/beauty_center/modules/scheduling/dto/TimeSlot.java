package beauty_center.modules.scheduling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Represents a time slot with start and end times.
 * Used for availability responses and booking requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlot {

    private OffsetDateTime startAt;
    private OffsetDateTime endAt;

}

