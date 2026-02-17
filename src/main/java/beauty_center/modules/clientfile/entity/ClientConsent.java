package beauty_center.modules.clientfile.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * ClientConsent entity tracking client notification preferences (SMS opt-in).
 * One consent record per client with unsubscribe token.
 */
@Entity
@Table(name = "client_consent")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientConsent {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(name = "client_id", nullable = false, columnDefinition = "UUID", unique = true)
    private UUID clientId;

    @Column(name = "sms_opt_in", nullable = false)
    @Builder.Default
    private boolean smsOptIn = true;

    @Column(name = "sms_unsub_token")
    private String smsUnsubToken;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
