package beauty_center.modules.notifications.sender;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Stub SMS sender for MVP — logs to console instead of sending real SMS.
 * Replace with Twilio, Vonage, or other provider for production.
 */
@Slf4j
@Component
public class StubSmsSender implements SmsSender {

    @Override
    public boolean send(String phoneNumber, String message) {
        log.info("=== SMS SENT (STUB) ===");
        log.info("To: {}", phoneNumber);
        log.info("Message: {}", message);
        log.info("========================");
        return true;
    }
}
