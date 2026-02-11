package beauty_center.modules.clientfile.controller;

import beauty_center.common.api.ApiResponse;
import beauty_center.modules.clientfile.dto.ClientConsentResponse;
import beauty_center.modules.clientfile.dto.ClientConsentUpdateRequest;
import beauty_center.modules.clientfile.entity.ClientConsent;
import beauty_center.modules.clientfile.service.ClientConsentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Client consent REST controller for SMS notification preferences.
 * Handles SMS opt-in/opt-out and public unsubscribe endpoint.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class ClientConsentController {

    private final ClientConsentService clientConsentService;

    /**
     * Get current client's consent preferences.
     * Creates default consent if doesn't exist.
     *
     * @return ClientConsentResponse
     */
    @GetMapping("/api/client/me/consent")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<ClientConsentResponse>> getMyConsent() {
        log.info("GET /api/client/me/consent - Getting current client's consent");

        ClientConsent consent = clientConsentService.getMyConsent();
        ClientConsentResponse response = clientConsentService.toResponse(consent);

        return ResponseEntity.ok(ApiResponse.ok(response, "Consent preferences retrieved successfully"));
    }

    /**
     * Update current client's consent preferences.
     *
     * @param request Update request with new preferences
     * @return Updated ClientConsentResponse
     */
    @PutMapping("/api/client/me/consent")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<ClientConsentResponse>> updateMyConsent(
            @RequestBody ClientConsentUpdateRequest request) {
        log.info("PUT /api/client/me/consent - Updating current client's consent");

        ClientConsent updated = clientConsentService.updateMyConsent(request);
        ClientConsentResponse response = clientConsentService.toResponse(updated);

        return ResponseEntity.ok(ApiResponse.ok(response, "Consent preferences updated successfully"));
    }

    /**
     * Public unsubscribe endpoint using SMS unsubscribe token.
     * No authentication required.
     *
     * @param token SMS unsubscribe token
     * @return Success message or 404 if token not found
     */
    @PostMapping("/api/consent/unsubscribe/{token}")
    public ResponseEntity<ApiResponse<Void>> unsubscribeBySmsToken(@PathVariable String token) {
        log.info("POST /api/consent/unsubscribe/{} - Public unsubscribe request", token);

        boolean success = clientConsentService.unsubscribeBySmsToken(token);

        if (!success) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("Invalid or expired unsubscribe token", 404));
        }

        return ResponseEntity.ok(ApiResponse.ok(null, "Successfully unsubscribed from SMS notifications"));
    }
}
