package beauty_center.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Structured error response containing details about what went wrong.
 * Uses OffsetDateTime for timezone-aware timestamp (ISO-8601 format in JSON).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    private int status;
    private String message;
    private String error;
    private String errorCode;  // Numeric/semantic error code for frontend handling
    private OffsetDateTime timestamp;
    private String path;
    private List<String> details;

}
