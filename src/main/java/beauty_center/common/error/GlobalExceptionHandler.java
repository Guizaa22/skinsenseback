package beauty_center.common.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Global exception handler for REST API.
 * Converts exceptions to standardized ApiError responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<String> details = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach(error ->
            details.add(error.getDefaultMessage())
        );

        ApiError apiError = ApiError.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message("Validation failed")
            .error("VALIDATION_ERROR")
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .details(details)
            .build();

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(apiError);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {

        ApiError apiError = ApiError.builder()
            .status(HttpStatus.FORBIDDEN.value())
            .message("Access denied")
            .error("ACCESS_DENIED")
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .build();

        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(apiError);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        ApiError apiError = ApiError.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message(ex.getMessage())
            .error("INVALID_ARGUMENT")
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .build();

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(apiError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        ApiError apiError = ApiError.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .message("Internal server error")
            .error("INTERNAL_ERROR")
            .timestamp(LocalDateTime.now())
            .path(request.getRequestURI())
            .build();

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(apiError);
    }

}
