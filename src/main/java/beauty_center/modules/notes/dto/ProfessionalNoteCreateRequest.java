package beauty_center.modules.notes.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a professional note after an appointment.
 * At least 'carePerformed' is required; other fields are optional.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalNoteCreateRequest {

    private String diagnostic;

    private String phototype;

    @NotBlank(message = "Care performed is required")
    private String carePerformed;

    private String productsAndParameters;

    private String reactions;

    private String recommendations;

    private String nextAppointmentSuggestion;
}
