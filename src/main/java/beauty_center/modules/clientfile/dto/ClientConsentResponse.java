package beauty_center.modules.clientfile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response DTO for client consent.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientConsentResponse {

    private UUID id;
    private UUID clientId;
    private boolean smsOptIn;
    private String smsUnsubToken;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

