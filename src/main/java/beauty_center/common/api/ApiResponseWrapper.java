package beauty_center.common.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard response wrapper for all API endpoints.
 * Wraps success or error responses with metadata.
 * When used for errors, returns ApiError in body directly.
 * When used for success, returns data in ApiResponse.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseWrapper<T> {

    private boolean success;
    private String message;
    private T data;
    private String timestamp;

    public static <T> ApiResponseWrapper<T> success(T data, String message) {
        return ApiResponseWrapper.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .build();
    }

    public static <T> ApiResponseWrapper<T> success(T data) {
        return success(data, "Success");
    }
}
