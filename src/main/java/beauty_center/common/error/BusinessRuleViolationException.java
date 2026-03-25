package beauty_center.common.error;

import lombok.Getter;

/**
 * Thrown when a business rule violation occurs (409 Conflict)
 * Examples: appointment overlap, invalid state transition, duplicate resource, etc.
 */
@Getter
public class BusinessRuleViolationException extends RuntimeException {

    private final String errorCode;

    public BusinessRuleViolationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessRuleViolationException(String message) {
        super(message);
        this.errorCode = "BUSINESS_RULE_VIOLATION";
    }

}
