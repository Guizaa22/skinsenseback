package beauty_center.modules.notifications.sender;

/**
 * Interface for email sending abstraction
 */
public interface EmailSender {

    /**
     * Send email
     */
    boolean send(String recipient, String subject, String body);

}
