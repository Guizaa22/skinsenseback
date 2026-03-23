package beauty_center.modules.notifications.sender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;

/**
 * SMTP email sender using Spring JavaMailSender + Brevo SMTP.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(JavaMailSender.class)
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;

    @Override
    public boolean send(String recipient, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("a56116001@smtp-brevo.com", "SkinSense Beauty Center");
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(body, false); // false = plain text, set true for HTML

            mailSender.send(message);

            log.info("=== EMAIL SENT via Brevo ===");
            log.info("To: {}", recipient);
            log.info("Subject: {}", subject);
            log.info("============================");
            return true;

        } catch (Exception e) {
            log.error("=== EMAIL FAILED ===");
            log.error("To: {}", recipient);
            log.error("Subject: {}", subject);
            log.error("Error: {}", e.getMessage());
            log.error("====================");
            return false;
        }
    }
}
