package beauty_center.modules.services.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Specialty response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialtyResponse {

    private UUID id;
    private String name;
    private String description;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

}

