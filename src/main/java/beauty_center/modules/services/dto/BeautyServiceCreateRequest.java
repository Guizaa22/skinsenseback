package beauty_center.modules.services.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Beauty service creation request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeautyServiceCreateRequest {

    @NotBlank(message = "Service name is required")
    private String name;

    private String description;

    @NotNull(message = "Duration is required")
    @Min(value = 15, message = "Minimum duration is 15 minutes")
    private Integer durationMinutes;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be positive")
    private BigDecimal price;

    // Optional specialty ID
    private UUID specialtyId;

    // Optional list of allowed employee IDs
    private List<UUID> allowedEmployeeIds;

    // Optional isActive flag (defaults to true)
    private Boolean isActive;

}
