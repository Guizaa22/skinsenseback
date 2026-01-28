package beauty_center.modules.audit.service;

import beauty_center.modules.audit.entity.AuditEntry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Audit service tracking sensitive operations and data access.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuditService {

    // TODO: Inject AuditRepository
    // TODO: Inject CurrentUser helper

    /**
     * Log an audit entry
     */
    public void log(AuditEntry entry) {
        // TODO: Enrich with current user and timestamp
        // TODO: Save to database
    }

    /**
     * Log CREATE operation
     */
    public void logCreate(String entityType, UUID entityId, String afterJson) {
        // TODO: Create audit entry
    }

    /**
     * Log UPDATE operation
     */
    public void logUpdate(String entityType, UUID entityId, String beforeJson, String afterJson) {
        // TODO: Create audit entry
    }

    /**
     * Log DELETE operation
     */
    public void logDelete(String entityType, UUID entityId, String beforeJson) {
        // TODO: Create audit entry
    }

    /**
     * Log sensitive data read
     */
    public void logSensitiveRead(String entityType, UUID entityId) {
        // TODO: Create audit entry for data access
    }

}
