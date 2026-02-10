package beauty_center.modules.auth.dto;

import beauty_center.modules.users.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Current authenticated user principal DTO.
 * Returned by GET /api/auth/me endpoint.
 * Contains user identification and role information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipalDto {

    private UUID id;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private boolean active;

}
