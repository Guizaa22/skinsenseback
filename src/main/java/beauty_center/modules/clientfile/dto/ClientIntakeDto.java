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
}

