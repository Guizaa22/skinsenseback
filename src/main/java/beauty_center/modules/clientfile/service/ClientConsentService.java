package beauty_center.modules.clientfile.service;

import beauty_center.modules.audit.service.AuditService;
import beauty_center.modules.clientfile.dto.ClientConsentResponse;
import beauty_center.modules.clientfile.dto.ClientConsentUpdateRequest;
import beauty_center.modules.clientfile.entity.ClientConsent;
import beauty_center.modules.clientfile.repository.ClientConsentRepository;
import beauty_center.modules.users.repository.UserAccountRepository;
import beauty_center.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Client consent service managing SMS notification preferences.
 * Handles SMS opt-in/opt-out and unsubscribe token management.
 * All operations are logged to audit trail.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ClientConsentService {

    private final ClientConsentRepository clientConsentRepository;
    private final UserAccountRepository userAccountRepository;
    private final AuditService auditService;
    private final CurrentUser currentUser;

    /**
     * Get current client's consent preferences.
     * Creates default consent if doesn't exist (smsOptIn=true, with token).
     *
     * @return ClientConsent for current client
     */
    public ClientConsent getMyConsent() {
        UUID clientId = currentUser.getUserId();
        if (clientId == null) {
            throw new AccessDeniedException("User not authenticated");
        }

        if (!currentUser.hasRole("CLIENT")) {
            throw new AccessDeniedException("Only clients can access their consent preferences");
        }

        Optional<ClientConsent> existingConsent = clientConsentRepository.findByClientId(clientId);
        if (existingConsent.isPresent()) {
            auditService.logSensitiveRead("ClientConsent", existingConsent.get().getId());
            return existingConsent.get();
        }

        // Create default consent if doesn't exist
        ClientConsent newConsent = ClientConsent.builder()
                .clientId(clientId)
                .smsOptIn(true)
                .smsUnsubToken(generateUnsubscribeToken())
                .build();

        ClientConsent saved = clientConsentRepository.save(newConsent);
        auditService.logCreate("ClientConsent", saved.getId(), saved);
        log.info("Client consent created: clientId={}, consentId={}", clientId, saved.getId());

        return saved;
    }

    /**
     * Update current client's consent preferences.
     *
     * @param request Update request with new preferences
     * @return Updated ClientConsent
     */
    public ClientConsent updateMyConsent(ClientConsentUpdateRequest request) {
        UUID clientId = currentUser.getUserId();
        if (clientId == null) {
            throw new AccessDeniedException("User not authenticated");
        }

        if (!currentUser.hasRole("CLIENT")) {
            throw new AccessDeniedException("Only clients can update their consent preferences");
        }

        // Get existing consent or create new one
        ClientConsent existingConsent = clientConsentRepository.findByClientId(clientId)
                .orElseGet(() -> {
                    ClientConsent newConsent = ClientConsent.builder()
                            .clientId(clientId)
                            .smsOptIn(true)
                            .smsUnsubToken(generateUnsubscribeToken())
                            .build();
                    return clientConsentRepository.save(newConsent);
                });

        // Capture before state for audit
        ClientConsent beforeState = cloneClientConsent(existingConsent);

        // Update smsOptIn if provided
        if (request.getSmsOptIn() != null) {
            existingConsent.setSmsOptIn(request.getSmsOptIn());
        }

        // Save updated consent
        ClientConsent updated = clientConsentRepository.save(existingConsent);

        // Log update operation
        auditService.logUpdate("ClientConsent", updated.getId(), beforeState, updated);
        log.info("Client consent updated: clientId={}, consentId={}, smsOptIn={}",
                clientId, updated.getId(), updated.isSmsOptIn());

        return updated;
    }

    /**
     * Unsubscribe from SMS notifications using unsubscribe token.
     * This is a public endpoint (no authentication required).
     *
     * @param token SMS unsubscribe token
     * @return true if unsubscribed successfully, false if token not found
     */
    public boolean unsubscribeBySmsToken(String token) {
        Optional<ClientConsent> consent = clientConsentRepository.findBySmsUnsubToken(token);

        if (consent.isEmpty()) {
            log.warn("Unsubscribe attempt with invalid token: {}", token);
            return false;
        }

        ClientConsent existingConsent = consent.get();

        // Capture before state for audit
        ClientConsent beforeState = cloneClientConsent(existingConsent);

        // Set smsOptIn to false
        existingConsent.setSmsOptIn(false);
        ClientConsent updated = clientConsentRepository.save(existingConsent);

        // Log update operation (no actor since this is public)
        auditService.logUpdate("ClientConsent", updated.getId(), beforeState, updated);
        log.info("Client unsubscribed via SMS token: clientId={}, consentId={}",
                updated.getClientId(), updated.getId());

        return true;
    }

    /**
     * Convert ClientConsent entity to ClientConsentResponse DTO.
     *
     * @param consent ClientConsent entity
     * @return ClientConsentResponse DTO
     */
    public ClientConsentResponse toResponse(ClientConsent consent) {
        if (consent == null) {
            return null;
        }

        return ClientConsentResponse.builder()
                .id(consent.getId())
                .clientId(consent.getClientId())
                .smsOptIn(consent.isSmsOptIn())
                .smsUnsubToken(consent.getSmsUnsubToken())
                .createdAt(consent.getCreatedAt())
                .updatedAt(consent.getUpdatedAt())
                .build();
    }

    // ===== Private Helper Methods =====

    /**
     * Generate secure unsubscribe token using UUID.
     *
     * @return UUID-based unsubscribe token
     */
    private String generateUnsubscribeToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Clone ClientConsent for audit trail (before state).
     */
    private ClientConsent cloneClientConsent(ClientConsent original) {
        return ClientConsent.builder()
                .id(original.getId())
                .clientId(original.getClientId())
                .smsOptIn(original.isSmsOptIn())
                .smsUnsubToken(original.getSmsUnsubToken())
                .createdAt(original.getCreatedAt())
                .updatedAt(original.getUpdatedAt())
                .build();
    }
}
