package beauty_center.modules.audit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Audit entry entity for tracking all sensitive operations.
 * Immutable audit trail for compliance and security.
 * Uses OffsetDateTime for timezone-aware timestamps.
 */
@Entity
@Table(name = "audit_entry")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEntry {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id = UUID.randomUUID();

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false, columnDefinition = "UUID")
    private UUID entityId;

    @Column(name = "action", nullable = false)
    private String action;  // CREATE, UPDATE, DELETE

    @Column(name = "actor_id", columnDefinition = "UUID")
    private UUID actorId;

    @Column(name = "at", nullable = false)
    private OffsetDateTime at;

    @Column(name = "before_json", columnDefinition = "TEXT")
    private String beforeJson;

    @Column(name = "after_json", columnDefinition = "TEXT")
    private String afterJson;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
        if (this.at == null) {
            this.at = OffsetDateTime.now();
        }
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }
}



