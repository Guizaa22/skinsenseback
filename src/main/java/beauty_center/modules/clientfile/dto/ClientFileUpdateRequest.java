package beauty_center.modules.clientfile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating client file.
 * Used by clients to update their declarative sections.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientFileUpdateRequest {

    // Intake Section
    private ClientIntakeDto intake;

    // Medical History Section
    private MedicalHistoryDto medicalHistory;

    // Aesthetic Procedure History Section
    private AestheticProcedureHistoryDto aestheticProcedureHistory;

    // Photo Consent
    private Boolean photoConsentForFollowup;
    private Boolean photoConsentForMarketing;
}

