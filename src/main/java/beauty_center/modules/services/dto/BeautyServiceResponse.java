package beauty_center.modules.services.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Beauty service response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeautyServiceResponse {

    private UUID id;
    private String name;
    private String description;
    private Integer durationMinutes;
    private BigDecimal price;
    private boolean isActive;
    private UUID specialtyId;
    private String specialtyName;
    private List<UUID> allowedEmployeeIds;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

}
