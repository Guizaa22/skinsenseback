package beauty_center.modules.notifications.sender;

import org.springframework.stereotype.Component;

/**
 * SMTP email sender implementation
 * TODO: Configure with actual mail server details
 */
@Component
public class SmtpEmailSender implements EmailSender {

    @Override
    public boolean send(String recipient, String subject, String body) {
        // TODO: Send email via SMTP
        // TODO: Handle provider responses
        // TODO: Log delivery
        return false;
    }

}
