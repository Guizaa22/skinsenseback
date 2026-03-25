package beauty_center.modules.notes.dto;

import beauty_center.modules.notes.entity.ProfessionalNote;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response DTO for professional note data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalNoteResponse {

    private UUID id;
    private UUID appointmentId;
    private UUID employeeId;
    private String diagnostic;
    private String phototype;
    private String carePerformed;
    private String productsAndParameters;
    private String reactions;
    private String recommendations;
    private String nextAppointmentSuggestion;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    /**
     * Map entity to response DTO.
     */
    public static ProfessionalNoteResponse fromEntity(ProfessionalNote note) {
        return ProfessionalNoteResponse.builder()
                .id(note.getId())
                .appointmentId(note.getAppointmentId())
                .employeeId(note.getEmployeeId())
                .diagnostic(note.getDiagnostic())
                .phototype(note.getPhototype())
                .carePerformed(note.getCarePerformed())
                .productsAndParameters(note.getProductsAndParameters())
                .reactions(note.getReactions())
                .recommendations(note.getRecommendations())
                .nextAppointmentSuggestion(note.getNextAppointmentSuggestion())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}
