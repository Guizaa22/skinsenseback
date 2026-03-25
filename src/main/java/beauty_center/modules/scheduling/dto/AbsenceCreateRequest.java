package beauty_center.modules.scheduling.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Absence creation request DTO.
 * Used by admin to create employee absences.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbsenceCreateRequest {

    @NotNull(message = "Start date/time is required")
    private OffsetDateTime startAt;

    @NotNull(message = "End date/time is required")
    private OffsetDateTime endAt;

    private String reason;

}

