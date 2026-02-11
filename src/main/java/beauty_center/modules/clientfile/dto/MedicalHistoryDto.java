package beauty_center.modules.clientfile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for medical history section.
 * Contains sensitive medical information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalHistoryDto {

    private String medicalBackground;
    private String currentTreatments;
    private String allergiesAndReactions;
}

