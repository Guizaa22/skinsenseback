package beauty_center.modules.notifications.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Notification message entity tracking individual sent notifications.
 * Records delivery status, provider response, and scheduling details.
 * Uses OffsetDateTime for timezone-aware timestamps.
 */
@Entity
@Table(name = "notification_message")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationMessage {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id = UUID.randomUUID();

    @Column(name = "client_id", nullable = false, columnDefinition = "UUID")
    private UUID clientId;

    @Column(name = "appointment_id", columnDefinition = "UUID")
    private UUID appointmentId;

    @Column(name = "type", nullable = false)
    private String type;  // BOOKING_CONFIRMATION, REMINDER_24H, REMINDER_2H

    @Column(name = "channel", nullable = false)
    private String channel;  // EMAIL, SMS

    @Column(name = "recipient", nullable = false)
    private String recipient;

    @Column(name = "scheduled_at")
    private OffsetDateTime scheduledAt;

    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

    @Column(name = "status", nullable = false)
    private String status;  // SCHEDULED, SENT, FAILED, CANCELED

    @Column(name = "provider_message_id")
    private String providerMessageId;

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
