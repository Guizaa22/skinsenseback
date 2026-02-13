package beauty_center.modules.notifications.controller;

import beauty_center.common.api.ApiResponse;
import beauty_center.modules.notifications.dto.NotificationMessageResponse;
import beauty_center.modules.notifications.dto.NotificationRuleCreateRequest;
import beauty_center.modules.notifications.dto.NotificationRuleResponse;
import beauty_center.modules.notifications.dto.NotificationRuleUpdateRequest;
import beauty_center.modules.notifications.repository.NotificationMessageRepository;
import beauty_center.modules.notifications.service.NotificationRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/notification-rules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class NotificationRuleController {

    private final NotificationRuleService ruleService;
    private final NotificationMessageRepository messageRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationRuleResponse>>> getAllRules() {
        List<NotificationRuleResponse> rules = ruleService.getAllRules();
        return ResponseEntity.ok(ApiResponse.ok(rules, "Notification rules retrieved"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationRuleResponse>> getRuleById(@PathVariable UUID id) {
        NotificationRuleResponse rule = ruleService.getRuleById(id);
        return ResponseEntity.ok(ApiResponse.ok(rule, "Notification rule retrieved"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NotificationRuleResponse>> createRule(
            @Valid @RequestBody NotificationRuleCreateRequest request) {
        NotificationRuleResponse rule = ruleService.createRule(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(rule, "Notification rule created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationRuleResponse>> updateRule(
            @PathVariable UUID id,
            @Valid @RequestBody NotificationRuleUpdateRequest request) {
        NotificationRuleResponse rule = ruleService.updateRule(id, request);
        return ResponseEntity.ok(ApiResponse.ok(rule, "Notification rule updated"));
    }

    /**
     * Get notification messages for an appointment (for admin debugging).
     */
    @GetMapping("/messages")
    public ResponseEntity<ApiResponse<List<NotificationMessageResponse>>> getMessages(
            @RequestParam(required = false) UUID appointmentId,
            @RequestParam(required = false) String status) {
        List<NotificationMessageResponse> messages;
        if (appointmentId != null) {
            messages = messageRepository.findByAppointmentId(appointmentId).stream()
                    .map(NotificationMessageResponse::fromEntity)
                    .collect(Collectors.toList());
        } else if (status != null) {
            messages = messageRepository.findByStatus(status).stream()
                    .map(NotificationMessageResponse::fromEntity)
                    .collect(Collectors.toList());
        } else {
            messages = messageRepository.findAll().stream()
                    .map(NotificationMessageResponse::fromEntity)
                    .collect(Collectors.toList());
        }
        return ResponseEntity.ok(ApiResponse.ok(messages, "Notification messages retrieved"));
    }
}
