package beauty_center.modules.notifications.sender;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Fallback email sender when SMTP is not configured.
 * Pairs with {@link SmtpEmailSender} which is only created when {@link JavaMailSender} exists.
 */
@Slf4j
@Component
@ConditionalOnMissingBean(JavaMailSender.class)
public class NoopEmailSender implements EmailSender {

    @Override
    public boolean send(String recipient, String subject, String body) {
        log.warn("Email not sent (mail not configured). To={}, subject={}", recipient, subject);
        return false;
    }
}
