package beauty_center.modules.clientfile.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating client consent preferences.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientConsentUpdateRequest {

    @NotNull(message = "SMS opt-in preference is required")
    private Boolean smsOptIn;
}

