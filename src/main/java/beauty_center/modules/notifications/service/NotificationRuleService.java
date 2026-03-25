package beauty_center.modules.notifications.service;

import beauty_center.common.error.EntityNotFoundException;
import beauty_center.modules.notifications.dto.NotificationRuleCreateRequest;
import beauty_center.modules.notifications.dto.NotificationRuleResponse;
import beauty_center.modules.notifications.dto.NotificationRuleUpdateRequest;
import beauty_center.modules.notifications.entity.NotificationRule;
import beauty_center.modules.notifications.repository.NotificationRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationRuleService {

    private final NotificationRuleRepository ruleRepository;

    @Transactional(readOnly = true)
    public List<NotificationRuleResponse> getAllRules() {
        return ruleRepository.findAll().stream()
                .map(NotificationRuleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public NotificationRuleResponse getRuleById(UUID id) {
        NotificationRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("NotificationRule", id));
        return NotificationRuleResponse.fromEntity(rule);
    }

    public NotificationRuleResponse createRule(NotificationRuleCreateRequest request) {
        NotificationRule rule = NotificationRule.builder()
                .id(UUID.randomUUID())
                .beautyServiceId(request.getBeautyServiceId())
                .type(request.getType())
                .channel(request.getChannel())
                .offsetHours(request.getOffsetHours())
                .isEnabled(request.getEnabled() != null ? request.getEnabled() : true)
                .build();

        NotificationRule saved = ruleRepository.save(rule);
        log.info("Notification rule created: id={}, type={}, channel={}", saved.getId(), saved.getType(), saved.getChannel());
        return NotificationRuleResponse.fromEntity(saved);
    }

    public NotificationRuleResponse updateRule(UUID id, NotificationRuleUpdateRequest request) {
        NotificationRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("NotificationRule", id));

        if (request.getEnabled() != null) {
            rule.setEnabled(request.getEnabled());
        }
        if (request.getOffsetHours() != null) {
            rule.setOffsetHours(request.getOffsetHours());
        }

        NotificationRule saved = ruleRepository.save(rule);
        log.info("Notification rule updated: id={}, enabled={}", saved.getId(), saved.isEnabled());
        return NotificationRuleResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<NotificationRule> getEnabledRulesForService(UUID beautyServiceId) {
        // Get service-specific rules + global rules (beautyServiceId is null)
        List<NotificationRule> serviceRules = ruleRepository.findByBeautyServiceIdAndIsEnabledTrue(beautyServiceId);
        List<NotificationRule> globalRules = ruleRepository.findByBeautyServiceIdIsNullAndIsEnabledTrue();
        globalRules.addAll(serviceRules);
        return globalRules;
    }
}
