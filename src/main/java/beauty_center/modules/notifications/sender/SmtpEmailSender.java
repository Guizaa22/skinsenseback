package beauty_center.modules.notifications.sender;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * SMTP email sender — logs to console for MVP.
 * Configure with MailHog or real SMTP for production.
 */
@Slf4j
@Component
public class SmtpEmailSender implements EmailSender {

    @Override
    public boolean send(String recipient, String subject, String body) {
        log.info("=== EMAIL SENT (STUB) ===");
        log.info("To: {}", recipient);
        log.info("Subject: {}", subject);
        log.info("Body: {}", body);
        log.info("==========================");
        return true;
    }
}
