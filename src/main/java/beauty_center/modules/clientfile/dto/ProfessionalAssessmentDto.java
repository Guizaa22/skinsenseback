package beauty_center.modules.clientfile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Professional assessment section - filled by the aesthetician only.
 * EMPLOYEE/ADMIN access only.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalAssessmentDto {

    private String diagnosticCutane;
    private String phototype; // Fitzpatrick I-VI
    private String indicationRetenue;
    private String soinRealise;
    private String produitsParametres;
    private String reactionsImmediates;
    private String recommandationsPostSoin;
    private String dateProchaineRdv;
}
