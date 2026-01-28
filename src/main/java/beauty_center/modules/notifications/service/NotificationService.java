package beauty_center.modules.notifications.service;

import beauty_center.modules.notifications.entity.NotificationMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * Notification service managing email and SMS delivery.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    // TODO: Inject NotificationRepository
    // TODO: Inject EmailSender
    // TODO: Inject SmsSender

    /**
     * Send email notification
     */
    public void sendEmail(String recipient, String subject, String body) {
        // TODO: Create notification record
        // TODO: Send via email provider
        // TODO: Update status
    }

    /**
     * Send SMS notification
     */
    public void sendSms(String phoneNumber, String message) {
        // TODO: Create notification record
        // TODO: Send via SMS provider
        // TODO: Update status
    }

    /**
     * Schedule notification for later
     */
    public void scheduleNotification(NotificationMessage notification) {
        // TODO: Save with SCHEDULED status
        // TODO: Queue for scheduling service
    }

}
