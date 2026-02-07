package beauty_center.common.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Global exception handler for REST API.
 * Converts exceptions to standardized ApiError responses with timezone-aware timestamps.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValidException(
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
            .errorCode("VALIDATION_FAILED")
            .timestamp(OffsetDateTime.now())
            .path(request.getRequestURI())
            .details(details)
            .build();

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(apiError);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiError> handleValidationException(
            ValidationException ex,
            HttpServletRequest request) {

        List<String> details = new ArrayList<>();
        if (ex.getFieldName() != null) {
            details.add(String.format("Field '%s' with value '%s' is invalid",
                ex.getFieldName(), ex.getRejectedValue()));
        }

        ApiError apiError = ApiError.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message(ex.getMessage())
            .error("VALIDATION_ERROR")
            .errorCode("VALIDATION_FAILED")
            .timestamp(OffsetDateTime.now())
            .path(request.getRequestURI())
            .details(details)
            .build();

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(apiError);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleEntityNotFoundException(
            EntityNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Entity not found: {}", ex.getMessage());

        ApiError apiError = ApiError.builder()
            .status(HttpStatus.NOT_FOUND.value())
            .message(ex.getMessage())
            .error("NOT_FOUND")
            .errorCode("ENTITY_NOT_FOUND")
            .timestamp(OffsetDateTime.now())
            .path(request.getRequestURI())
            .build();

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(apiError);
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ApiError> handleBusinessRuleViolationException(
            BusinessRuleViolationException ex,
            HttpServletRequest request) {

        log.warn("Business rule violated: {}", ex.getMessage());

        ApiError apiError = ApiError.builder()
            .status(HttpStatus.CONFLICT.value())
            .message(ex.getMessage())
            .error("CONFLICT")
            .errorCode(ex.getErrorCode())
            .timestamp(OffsetDateTime.now())
            .path(request.getRequestURI())
            .build();

        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(apiError);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request) {

        log.warn("Authentication failed: {}", ex.getMessage());

        ApiError apiError = ApiError.builder()
            .status(HttpStatus.UNAUTHORIZED.value())
            .message("Authentication failed")
            .error("UNAUTHORIZED")
            .errorCode("AUTHENTICATION_FAILED")
            .timestamp(OffsetDateTime.now())
            .path(request.getRequestURI())
            .build();

        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(apiError);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {

        log.warn("Access denied: {}", ex.getMessage());

        ApiError apiError = ApiError.builder()
            .status(HttpStatus.FORBIDDEN.value())
            .message("Access denied")
            .error("FORBIDDEN")
            .errorCode("ACCESS_DENIED")
            .timestamp(OffsetDateTime.now())
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

        log.warn("Invalid argument: {}", ex.getMessage());

        ApiError apiError = ApiError.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message(ex.getMessage())
            .error("INVALID_ARGUMENT")
            .errorCode("INVALID_ARGUMENT")
            .timestamp(OffsetDateTime.now())
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

        log.error("Unexpected error occurred", ex);

        ApiError apiError = ApiError.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .message("Internal server error")
            .error("INTERNAL_ERROR")
            .errorCode("INTERNAL_SERVER_ERROR")
            .timestamp(OffsetDateTime.now())
            .path(request.getRequestURI())
            .build();

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(apiError);
    }

}
