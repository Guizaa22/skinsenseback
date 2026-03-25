package beauty_center.modules.scheduling.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Employee update request DTO.
 * Used by admin to update employee information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeUpdateRequest {

    private String fullName;

    @Email(message = "Email should be valid")
    private String email;

    private String phone;

    private Boolean isActive;

    /**
     * List of specialty IDs this employee has
     */
    private List<UUID> specialtyIds;

}

