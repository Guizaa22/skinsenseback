package beauty_center.modules.scheduling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Absence response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbsenceResponse {

    private UUID id;
    private UUID employeeId;
    private OffsetDateTime startAt;
    private OffsetDateTime endAt;
    private String reason;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

}

