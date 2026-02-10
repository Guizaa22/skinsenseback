package beauty_center.modules.services.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Specialty creation request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialtyCreateRequest {

    @NotBlank(message = "Specialty name is required")
    private String name;

    private String description;

}

