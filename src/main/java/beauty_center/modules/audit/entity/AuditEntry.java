package beauty_center.modules.audit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Audit entry entity for tracking all sensitive operations.
 * Immutable audit trail for compliance and security.
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

    @Column(name = "actor_id", columnDefinition = "UUID")
    private UUID actorId;

    @Column(name = "entity_type", nullable = false)
    private String entityType; // Class name or entity type

    @Column(name = "entity_id", columnDefinition = "UUID")
    private UUID entityId;

    @Column(name = "action", nullable = false)
    private String action; // CREATE, UPDATE, DELETE, READ_SENSITIVE

    @Column(name = "before_json", columnDefinition = "TEXT")
    private String beforeJson;

    @Column(name = "after_json", columnDefinition = "TEXT")
    private String afterJson;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }

}
