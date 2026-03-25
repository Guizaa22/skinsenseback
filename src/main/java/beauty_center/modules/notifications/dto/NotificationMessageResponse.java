package beauty_center.modules.notifications.dto;

import beauty_center.modules.notifications.entity.NotificationMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessageResponse {
    private UUID id;
    private UUID clientId;
    private UUID appointmentId;
    private String type;
    private String channel;
    private String recipient;
    private OffsetDateTime scheduledAt;
    private OffsetDateTime sentAt;
    private String status;
    private OffsetDateTime createdAt;

    public static NotificationMessageResponse fromEntity(NotificationMessage msg) {
        return NotificationMessageResponse.builder()
                .id(msg.getId())
                .clientId(msg.getClientId())
                .appointmentId(msg.getAppointmentId())
                .type(msg.getType())
                .channel(msg.getChannel())
                .recipient(msg.getRecipient())
                .scheduledAt(msg.getScheduledAt())
                .sentAt(msg.getSentAt())
                .status(msg.getStatus())
                .createdAt(msg.getCreatedAt())
                .build();
    }
}
