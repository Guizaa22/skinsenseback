package beauty_center.modules.notifications.repository;

import beauty_center.modules.notifications.entity.NotificationMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationMessageRepository extends JpaRepository<NotificationMessage, UUID> {

    List<NotificationMessage> findByAppointmentId(UUID appointmentId);

    List<NotificationMessage> findByClientId(UUID clientId);

    @Query("SELECT m FROM NotificationMessage m WHERE m.scheduledAt <= :now AND m.status = 'SCHEDULED' ORDER BY m.scheduledAt")
    List<NotificationMessage> findDueMessages(@Param("now") OffsetDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE NotificationMessage m SET m.status = 'CANCELED' WHERE m.appointmentId = :appointmentId AND m.status = 'SCHEDULED'")
    int cancelScheduledByAppointmentId(@Param("appointmentId") UUID appointmentId);

    List<NotificationMessage> findByStatus(String status);
}
