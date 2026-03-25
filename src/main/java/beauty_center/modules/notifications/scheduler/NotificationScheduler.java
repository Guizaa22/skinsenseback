package beauty_center.modules.notifications.scheduler;

import beauty_center.modules.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job that processes due notification messages every minute.
 * Implements the outbox pattern: fetch SCHEDULED messages, send, update status.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;

    /**
     * Runs every 60 seconds. Processes all due notifications.
     */
    @Scheduled(fixedRate = 60000)
    public void processNotifications() {
        log.debug("Notification scheduler running...");
        try {
            notificationService.processDueNotifications();
        } catch (Exception e) {
            log.error("Error in notification scheduler: {}", e.getMessage(), e);
        }
    }
}
