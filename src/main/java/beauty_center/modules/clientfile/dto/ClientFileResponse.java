package beauty_center.modules.clientfile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response DTO for client file.
 * Contains all sections of the client file.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientFileResponse {

    private UUID id;
    private UUID clientId;

    // Intake Section
    private ClientIntakeDto intake;

    // Medical History Section
    private MedicalHistoryDto medicalHistory;

    // Aesthetic Procedure History Section
    private AestheticProcedureHistoryDto aestheticProcedureHistory;

    // Professional Assessment Section (EMPLOYEE/ADMIN only)
    private ProfessionalAssessmentDto professionalAssessment;

    // Photo Consent
    private boolean photoConsentForFollowup;
    private boolean photoConsentForMarketing;

    // Timestamps
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

