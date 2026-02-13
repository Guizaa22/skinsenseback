package beauty_center.modules.audit.controller;

import beauty_center.common.api.ApiResponse;
import beauty_center.common.error.EntityNotFoundException;
import beauty_center.modules.audit.entity.AuditEntry;
import beauty_center.modules.audit.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Audit REST controller (ADMIN only).
 * Provides read access to the immutable audit trail.
 */
@RestController
@RequestMapping("/api/admin/audit")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final AuditRepository auditRepository;

    /**
     * Get audit entries with optional filters.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AuditEntry>>> getAuditEntries(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) UUID entityId,
            @RequestParam(required = false) String action) {

        List<AuditEntry> entries;

        if (entityType != null && entityId != null && action != null) {
            entries = auditRepository.findByEntityTypeAndEntityIdAndAction(entityType, entityId, action);
        } else if (entityType != null && entityId != null) {
            entries = auditRepository.findByEntityTypeAndEntityId(entityType, entityId);
        } else if (entityType != null) {
            entries = auditRepository.findByEntityType(entityType);
        } else if (action != null) {
            entries = auditRepository.findByAction(action);
        } else {
            entries = auditRepository.findAll();
        }

        return ResponseEntity.ok(ApiResponse.ok(entries, "Audit entries retrieved"));
    }

    /**
     * Get specific audit entry by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuditEntry>> getAuditEntry(@PathVariable UUID id) {
        AuditEntry entry = auditRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("AuditEntry", id));
        return ResponseEntity.ok(ApiResponse.ok(entry, "Audit entry retrieved"));
    }
}
