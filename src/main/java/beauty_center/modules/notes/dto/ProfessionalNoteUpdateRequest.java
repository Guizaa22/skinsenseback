package beauty_center.modules.notes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating a professional note.
 * All fields are optional — only non-null values are applied.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalNoteUpdateRequest {

    private String diagnostic;

    private String phototype;

    private String carePerformed;

    private String productsAndParameters;

    private String reactions;

    private String recommendations;

    private String nextAppointmentSuggestion;
}
