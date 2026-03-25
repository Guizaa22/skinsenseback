package beauty_center.common.error;

import lombok.Getter;

/**
 * Thrown when a requested entity is not found (404 Not Found)
 */
@Getter
public class EntityNotFoundException extends RuntimeException {

    private final String entityType;
    private final Object entityId;

    public EntityNotFoundException(String entityType, Object entityId) {
        super(String.format("%s with id %s not found", entityType, entityId));
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public EntityNotFoundException(String message) {
        super(message);
        this.entityType = null;
        this.entityId = null;
    }

}
