package beauty_center.modules.audit.service;

import beauty_center.modules.audit.entity.AuditEntry;
import beauty_center.modules.audit.repository.AuditRepository;
import beauty_center.security.CurrentUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Audit service tracking sensitive operations and data access.
 * Logs all CREATE, UPDATE, DELETE, and READ operations on sensitive entities.
 * Uses JSON serialization for entity snapshots.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuditService {

    private final AuditRepository auditRepository;
    private final CurrentUser currentUser;
    private final ObjectMapper objectMapper;

    /**
     * Log an audit entry with current user and timestamp enrichment.
     *
     * @param entry Audit entry to log
     */
    public void log(AuditEntry entry) {
        // Enrich with current user if not already set
        if (entry.getActorId() == null) {
            entry.setActorId(currentUser.getUserId());
        }

        // Enrich with timestamp if not already set
        if (entry.getAt() == null) {
            entry.setAt(OffsetDateTime.now());
        }

        // Save to database
        auditRepository.save(entry);
        log.info("Audit entry created: entityType={}, entityId={}, action={}, actorId={}",
                entry.getEntityType(), entry.getEntityId(), entry.getAction(), entry.getActorId());
    }

    /**
     * Log CREATE operation.
     *
     * @param entityType Type of entity (e.g., "ClientFile", "ClientConsent")
     * @param entityId   UUID of the created entity
     * @param afterJson  JSON representation of the created entity
     */
    public void logCreate(String entityType, UUID entityId, String afterJson) {
        AuditEntry entry = AuditEntry.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action("CREATE")
                .actorId(currentUser.getUserId())
                .at(OffsetDateTime.now())
                .afterJson(afterJson)
                .build();

        log(entry);
    }

    /**
     * Log CREATE operation with object serialization.
     *
     * @param entityType  Type of entity
     * @param entityId    UUID of the created entity
     * @param afterObject Object to serialize to JSON
     */
    public void logCreate(String entityType, UUID entityId, Object afterObject) {
        String afterJson = serializeToJson(afterObject);
        logCreate(entityType, entityId, afterJson);
    }

    /**
     * Log UPDATE operation.
     *
     * @param entityType Type of entity
     * @param entityId   UUID of the updated entity
     * @param beforeJson JSON representation before update
     * @param afterJson  JSON representation after update
     */
    public void logUpdate(String entityType, UUID entityId, String beforeJson, String afterJson) {
        AuditEntry entry = AuditEntry.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action("UPDATE")
                .actorId(currentUser.getUserId())
                .at(OffsetDateTime.now())
                .beforeJson(beforeJson)
                .afterJson(afterJson)
                .build();

        log(entry);
    }

    /**
     * Log UPDATE operation with object serialization.
     *
     * @param entityType   Type of entity
     * @param entityId     UUID of the updated entity
     * @param beforeObject Object before update
     * @param afterObject  Object after update
     */
    public void logUpdate(String entityType, UUID entityId, Object beforeObject, Object afterObject) {
        String beforeJson = serializeToJson(beforeObject);
        String afterJson = serializeToJson(afterObject);
        logUpdate(entityType, entityId, beforeJson, afterJson);
    }

    /**
     * Log DELETE operation.
     *
     * @param entityType Type of entity
     * @param entityId   UUID of the deleted entity
     * @param beforeJson JSON representation before deletion
     */
    public void logDelete(String entityType, UUID entityId, String beforeJson) {
        AuditEntry entry = AuditEntry.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action("DELETE")
                .actorId(currentUser.getUserId())
                .at(OffsetDateTime.now())
                .beforeJson(beforeJson)
                .build();

        log(entry);
    }

    /**
     * Log DELETE operation with object serialization.
     *
     * @param entityType   Type of entity
     * @param entityId     UUID of the deleted entity
     * @param beforeObject Object before deletion
     */
    public void logDelete(String entityType, UUID entityId, Object beforeObject) {
        String beforeJson = serializeToJson(beforeObject);
        logDelete(entityType, entityId, beforeJson);
    }

    /**
     * Log sensitive data read operation.
     *
     * @param entityType Type of entity
     * @param entityId   UUID of the entity being read
     */
    public void logSensitiveRead(String entityType, UUID entityId) {
        AuditEntry entry = AuditEntry.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action("READ")
                .actorId(currentUser.getUserId())
                .at(OffsetDateTime.now())
                .build();

        log(entry);
    }

    /**
     * Serialize object to JSON string.
     * Returns null if serialization fails.
     *
     * @param object Object to serialize
     * @return JSON string or null
     */
    private String serializeToJson(Object object) {
        if (object == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object to JSON: {}", e.getMessage());
            return null;
        }
    }
}
