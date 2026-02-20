package beauty_center.common.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard error response wrapper maintaining consistency with ApiResponse.
 * Used by GlobalExceptionHandler to ensure all errors follow the same contract.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    @Builder.Default
    private boolean success = false;
    private int status;
    private String message;
    private String error;
    private String errorCode;
    private String path;

    /**
     * Factory method to create ErrorResponse
     */
    public static ErrorResponse of(int status, String message, String error, String errorCode) {
        return ErrorResponse.builder()
            .success(false)
            .status(status)
            .message(message)
            .error(error)
            .errorCode(errorCode)
            .build();
    }
}
