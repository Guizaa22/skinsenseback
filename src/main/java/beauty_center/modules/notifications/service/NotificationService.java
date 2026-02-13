package beauty_center.modules.notifications.service;

import beauty_center.modules.appointments.entity.Appointment;
import beauty_center.modules.clientfile.entity.ClientConsent;
import beauty_center.modules.clientfile.repository.ClientConsentRepository;
import beauty_center.modules.notifications.entity.NotificationMessage;
import beauty_center.modules.notifications.entity.NotificationRule;
import beauty_center.modules.notifications.repository.NotificationMessageRepository;
import beauty_center.modules.notifications.sender.EmailSender;
import beauty_center.modules.notifications.sender.SmsSender;
import beauty_center.modules.users.entity.UserAccount;
import beauty_center.modules.users.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Notification service implementing the outbox pattern.
 * Schedules notification messages when events occur (appointment created/canceled),
 * and processes due messages via the scheduler.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationMessageRepository messageRepository;
    private final NotificationRuleService ruleService;
    private final UserAccountRepository userAccountRepository;
    private final ClientConsentRepository clientConsentRepository;
    private final EmailSender emailSender;
    private final SmsSender smsSender;

    /**
     * Schedule notifications for a newly created appointment based on enabled rules.
     */
    public void scheduleNotificationsForAppointment(Appointment appointment) {
        UUID serviceId = appointment.getBeautyServiceId();
        List<NotificationRule> rules = ruleService.getEnabledRulesForService(serviceId);

        UserAccount client = userAccountRepository.findById(appointment.getClientId()).orElse(null);
        if (client == null) {
            log.warn("Client not found for appointment {}, skipping notifications", appointment.getId());
            return;
        }

        for (NotificationRule rule : rules) {
            String recipient = resolveRecipient(client, rule.getChannel());
            if (recipient == null) {
                log.debug("No recipient for channel {} for client {}", rule.getChannel(), client.getId());
                continue;
            }

            OffsetDateTime scheduledAt = calculateScheduledAt(appointment, rule);

            NotificationMessage message = NotificationMessage.builder()
                    .id(UUID.randomUUID())
                    .clientId(client.getId())
                    .appointmentId(appointment.getId())
                    .type(rule.getType())
                    .channel(rule.getChannel())
                    .recipient(recipient)
                    .scheduledAt(scheduledAt)
                    .status("SCHEDULED")
                    .build();

            messageRepository.save(message);
            log.info("Notification scheduled: type={}, channel={}, appointment={}, scheduledAt={}",
                    rule.getType(), rule.getChannel(), appointment.getId(), scheduledAt);
        }
    }

    /**
     * Cancel all pending (SCHEDULED) notifications for an appointment.
     */
    public void cancelNotificationsForAppointment(UUID appointmentId) {
        int canceled = messageRepository.cancelScheduledByAppointmentId(appointmentId);
        log.info("Canceled {} scheduled notifications for appointment {}", canceled, appointmentId);
    }

    /**
     * Process all due notifications (called by scheduler).
     * Fetches SCHEDULED messages where scheduledAt <= now, sends them, updates status.
     */
    public void processDueNotifications() {
        List<NotificationMessage> dueMessages = messageRepository.findDueMessages(OffsetDateTime.now());

        if (dueMessages.isEmpty()) {
            return;
        }

        log.info("Processing {} due notifications", dueMessages.size());

        for (NotificationMessage message : dueMessages) {
            try {
                // Check SMS consent before sending SMS
                if ("SMS".equals(message.getChannel()) && !hasSmsConsent(message.getClientId())) {
                    log.info("SMS consent not given for client {}, skipping message {}", message.getClientId(), message.getId());
                    message.setStatus("CANCELED");
                    message.setUpdatedAt(OffsetDateTime.now());
                    messageRepository.save(message);
                    continue;
                }

                boolean sent = sendMessage(message);

                if (sent) {
                    message.setStatus("SENT");
                    message.setSentAt(OffsetDateTime.now());
                } else {
                    message.setStatus("FAILED");
                }
                message.setUpdatedAt(OffsetDateTime.now());
                messageRepository.save(message);

            } catch (Exception e) {
                log.error("Failed to process notification {}: {}", message.getId(), e.getMessage());
                message.setStatus("FAILED");
                message.setUpdatedAt(OffsetDateTime.now());
                messageRepository.save(message);
            }
        }
    }

    // ===== Private Helpers =====

    private boolean sendMessage(NotificationMessage message) {
        String subject = buildSubject(message.getType());
        String body = buildBody(message.getType(), message.getAppointmentId());

        return switch (message.getChannel()) {
            case "EMAIL" -> emailSender.send(message.getRecipient(), subject, body);
            case "SMS" -> smsSender.send(message.getRecipient(), body);
            default -> {
                log.warn("Unknown channel: {}", message.getChannel());
                yield false;
            }
        };
    }

    private String resolveRecipient(UserAccount client, String channel) {
        return switch (channel) {
            case "EMAIL" -> client.getEmail();
            case "SMS" -> client.getPhone();
            default -> null;
        };
    }

    private OffsetDateTime calculateScheduledAt(Appointment appointment, NotificationRule rule) {
        if ("BOOKING_CONFIRMATION".equals(rule.getType())) {
            return OffsetDateTime.now(); // Send immediately
        }
        if (rule.getOffsetHours() != null) {
            return appointment.getStartAt().plusHours(rule.getOffsetHours());
        }
        return OffsetDateTime.now();
    }

    private boolean hasSmsConsent(UUID clientId) {
        Optional<ClientConsent> consent = clientConsentRepository.findByClientId(clientId);
        return consent.map(ClientConsent::isSmsOptIn).orElse(true); // Default to true if no consent record
    }

    private String buildSubject(String type) {
        return switch (type) {
            case "BOOKING_CONFIRMATION" -> "Booking Confirmation - Beauty & Care Center";
            case "REMINDER_24H" -> "Appointment Reminder (24h) - Beauty & Care Center";
            case "REMINDER_2H" -> "Appointment Reminder (2h) - Beauty & Care Center";
            default -> "Notification - Beauty & Care Center";
        };
    }

    private String buildBody(String type, UUID appointmentId) {
        return switch (type) {
            case "BOOKING_CONFIRMATION" -> "Your appointment has been confirmed. Appointment ID: " + appointmentId;
            case "REMINDER_24H" -> "Reminder: You have an appointment tomorrow. Appointment ID: " + appointmentId;
            case "REMINDER_2H" -> "Reminder: Your appointment is in 2 hours. Appointment ID: " + appointmentId;
            default -> "You have a notification regarding your appointment. ID: " + appointmentId;
        };
    }
}
