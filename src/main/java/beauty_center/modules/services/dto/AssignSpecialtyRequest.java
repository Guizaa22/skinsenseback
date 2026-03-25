package beauty_center.modules.services.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for assigning specialty to employee
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignSpecialtyRequest {

    @NotNull(message = "Specialty ID is required")
    private UUID specialtyId;

}

