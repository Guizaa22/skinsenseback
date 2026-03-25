package beauty_center.modules.notifications.config;

import beauty_center.modules.notifications.entity.NotificationRule;
import beauty_center.modules.notifications.repository.NotificationRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.UUID;

/**
 * Seeds default notification rules in dev/test profiles.
 */
@Configuration
@Profile({"dev", "test"})
@RequiredArgsConstructor
@Slf4j
public class NotificationBootstrapConfig {

    private final NotificationRuleRepository ruleRepository;

    @Bean
    public CommandLineRunner bootstrapNotificationRules() {
        return args -> {
            if (ruleRepository.count() > 0) {
                log.debug("Notification rules already exist, skipping bootstrap");
                return;
            }

            log.info("=== Bootstrapping Notification Rules ===");

            // Booking confirmation via EMAIL (sent immediately)
            ruleRepository.save(NotificationRule.builder()
                    .id(UUID.randomUUID())
                    .type("BOOKING_CONFIRMATION")
                    .channel("EMAIL")
                    .offsetHours(0)
                    .isEnabled(true)
                    .build());
            log.info("Created rule: BOOKING_CONFIRMATION via EMAIL");

            // 24h reminder via EMAIL
            ruleRepository.save(NotificationRule.builder()
                    .id(UUID.randomUUID())
                    .type("REMINDER_24H")
                    .channel("EMAIL")
                    .offsetHours(-24)
                    .isEnabled(true)
                    .build());
            log.info("Created rule: REMINDER_24H via EMAIL");

            // 2h reminder via SMS
            ruleRepository.save(NotificationRule.builder()
                    .id(UUID.randomUUID())
                    .type("REMINDER_2H")
                    .channel("SMS")
                    .offsetHours(-2)
                    .isEnabled(true)
                    .build());
            log.info("Created rule: REMINDER_2H via SMS");

            log.info("=== Notification Rules Bootstrap Complete ===");
        };
    }
}
