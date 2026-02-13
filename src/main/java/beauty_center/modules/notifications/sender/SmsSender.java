package beauty_center.modules.notifications.sender;

/**
 * Interface for SMS sending abstraction.
 */
public interface SmsSender {

    /**
     * Send an SMS message.
     *
     * @param phoneNumber Recipient phone number
     * @param message     SMS body
     * @return true if sent successfully, false otherwise
     */
    boolean send(String phoneNumber, String message);
}
