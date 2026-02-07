package beauty_center.common.error;

/**
 * Thrown when request validation fails (400 Bad Request)
 * More specific than IllegalArgumentException for API validation scenarios.
 */
public class ValidationException extends RuntimeException {

    private final String fieldName;
    private final Object rejectedValue;

    public ValidationException(String message, String fieldName, Object rejectedValue) {
        super(message);
        this.fieldName = fieldName;
        this.rejectedValue = rejectedValue;
    }

    public ValidationException(String message) {
        super(message);
        this.fieldName = null;
        this.rejectedValue = null;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getRejectedValue() {
        return rejectedValue;
    }
}
