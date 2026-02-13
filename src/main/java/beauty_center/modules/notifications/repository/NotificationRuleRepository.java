package beauty_center.modules.notifications.repository;

import beauty_center.modules.notifications.entity.NotificationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRuleRepository extends JpaRepository<NotificationRule, UUID> {

    List<NotificationRule> findByIsEnabledTrue();

    List<NotificationRule> findByBeautyServiceIdAndIsEnabledTrue(UUID beautyServiceId);

    List<NotificationRule> findByBeautyServiceIdIsNullAndIsEnabledTrue();

    List<NotificationRule> findByType(String type);

    List<NotificationRule> findByChannel(String channel);
}
