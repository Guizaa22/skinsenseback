package beauty_center.modules.clientfile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for client intake section.
 * Contains information about how client found us and their objectives.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientIntakeDto {

    private String howDidYouHearAboutUs;
    private String consultationReason;
    private String objective;
    private String careType;
    private String skincareRoutine;
    private String habits;

    private String dateOfBirth;
    private String address;
    private String profession;

    /** How they found Skinsense (comma-separated or free text) */
    private String howFoundUs;

    /** Consultation type checkboxes (comma-separated selected values): nettoyagePeau, hydraFacial, microneedling, autre */
    private String consultationTypes;
    /** Free text if autre selected */
    private String consultationTypeAutre;
}

