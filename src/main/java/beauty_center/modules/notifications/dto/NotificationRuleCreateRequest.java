package beauty_center.modules.notifications.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRuleCreateRequest {

    private UUID beautyServiceId; // null = global rule

    @NotBlank(message = "Type is required")
    @Pattern(regexp = "BOOKING_CONFIRMATION|REMINDER_24H|REMINDER_2H", message = "Type must be BOOKING_CONFIRMATION, REMINDER_24H, or REMINDER_2H")
    private String type;

    @NotBlank(message = "Channel is required")
    @Pattern(regexp = "EMAIL|SMS", message = "Channel must be EMAIL or SMS")
    private String channel;

    private Integer offsetHours; // e.g. -24, -2 for reminders

    @Builder.Default
    private Boolean enabled = true;
}
