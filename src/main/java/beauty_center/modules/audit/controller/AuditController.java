package beauty_center.modules.audit.controller;

import beauty_center.common.api.ApiResponse;
import beauty_center.modules.audit.entity.AuditEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Audit REST controller (admin only)
 */
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    // TODO: Inject AuditService

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAuditEntries(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) UUID entityId,
            @RequestParam(required = false) String action) {
        // TODO: Get audit entries with filters
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuditEntry>> getAuditEntry(@PathVariable UUID id) {
        // TODO: Get specific audit entry
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

}
