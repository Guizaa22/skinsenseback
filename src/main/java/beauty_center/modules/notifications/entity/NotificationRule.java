package beauty_center.modules.notifications.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * NotificationRule entity defining templates for automatic notifications.
 * Specifies when and how notifications should be sent (e.g., 24h reminder via SMS).
 */
@Entity
@Table(name = "notification_rule")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRule {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(name = "beauty_service_id", columnDefinition = "UUID")
    private UUID beautyServiceId;

    @Column(name = "type", nullable = false)
    private String type;  // BOOKING_CONFIRMATION, REMINDER_24H, REMINDER_2H

    @Column(name = "channel", nullable = false)
    private String channel;  // EMAIL, SMS

    @Column(name = "offset_hours")
    private Integer offsetHours;  // for reminders: -24, -2, etc.

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private boolean isEnabled = true;

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
