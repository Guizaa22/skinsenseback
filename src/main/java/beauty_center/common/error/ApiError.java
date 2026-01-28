package beauty_center.common.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Structured error response containing details about what went wrong.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {

    private int status;
    private String message;
    private String error;
    private LocalDateTime timestamp;
    private String path;
    private List<String> details;

}
