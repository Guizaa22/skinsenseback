package beauty_center.modules.audit.repository;

import beauty_center.modules.audit.entity.AuditEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for AuditEntry persistence operations.
 * Manages immutable audit trail for compliance and security.
 */
@Repository
public interface AuditRepository extends JpaRepository<AuditEntry, UUID> {

    /**
     * Find all audit entries for a specific entity type.
     *
     * @param entityType Type of entity (e.g., "ClientFile", "Appointment")
     * @return List of audit entries
     */
    List<AuditEntry> findByEntityType(String entityType);

    /**
     * Find all audit entries for a specific entity.
     *
     * @param entityType Type of entity
     * @param entityId UUID of the entity
     * @return List of audit entries
     */
    List<AuditEntry> findByEntityTypeAndEntityId(String entityType, UUID entityId);

    /**
     * Find all audit entries for a specific action.
     *
     * @param action Action type (e.g., "CREATE", "UPDATE", "DELETE", "READ")
     * @return List of audit entries
     */
    List<AuditEntry> findByAction(String action);

    /**
     * Find all audit entries by actor (user who performed the action).
     *
     * @param actorId UUID of the user who performed the action
     * @return List of audit entries
     */
    List<AuditEntry> findByActorId(UUID actorId);

    /**
     * Find all audit entries for a specific entity and action.
     *
     * @param entityType Type of entity
     * @param entityId UUID of the entity
     * @param action Action type
     * @return List of audit entries
     */
    List<AuditEntry> findByEntityTypeAndEntityIdAndAction(String entityType, UUID entityId, String action);
}

