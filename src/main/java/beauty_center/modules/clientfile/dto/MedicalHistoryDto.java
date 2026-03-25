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

    // Medical checkboxes
    private Boolean grossesse;
    private Boolean diabete;
    private Boolean maladieAutoImmune;
    private Boolean troublesHormonaux;
    private Boolean problemsCicatrisation;
    private Boolean herpes;
    private Boolean epilepsie;
    private Boolean cancer;
    private Boolean maladiesDermatologiques;
    private Boolean interventionChirurgicale;
    private String autreAntecedent;

    // Medications checkboxes
    private Boolean isotretinoine;
    private Boolean corticoides;
    private Boolean anticoagulants;
    private Boolean hormones;
    private Boolean antibiotiques;
    private String dateArretMedicament;

    // Allergies detail fields
    private Boolean allergiesMedicamenteuses;
    private Boolean allergiesCosmetiques;
    private Boolean reactionsPostSoinsAnterieures;
    private String detailsAllergies;

    // Lifestyle checkboxes
    private Boolean tabac;
    private Boolean expositionSolaire;
    private Boolean cabineUV;
    private Boolean sportIntensif;
    private Boolean stressImportant;
}

