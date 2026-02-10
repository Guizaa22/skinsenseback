package beauty_center.modules.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication error response DTO.
 * Returned for 401/403 authentication and authorization failures.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthErrorResponse {

    private String error;
    private String message;
    private int status;
    private long timestamp;

    public static AuthErrorResponse of(String error, String message, int status) {
        return AuthErrorResponse.builder()
            .error(error)
            .message(message)
            .status(status)
            .timestamp(System.currentTimeMillis())
            .build();
    }

}
