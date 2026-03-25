package beauty_center.modules.notifications.dto;

import beauty_center.modules.notifications.entity.NotificationRule;
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
public class NotificationRuleResponse {
    private UUID id;
    private UUID beautyServiceId;
    private String type;
    private String channel;
    private Integer offsetHours;
    private boolean enabled;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static NotificationRuleResponse fromEntity(NotificationRule rule) {
        return NotificationRuleResponse.builder()
                .id(rule.getId())
                .beautyServiceId(rule.getBeautyServiceId())
                .type(rule.getType())
                .channel(rule.getChannel())
                .offsetHours(rule.getOffsetHours())
                .enabled(rule.isEnabled())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }
}
