package beauty_center.modules.notifications.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRuleUpdateRequest {
    private Boolean enabled;
    private Integer offsetHours;
}
