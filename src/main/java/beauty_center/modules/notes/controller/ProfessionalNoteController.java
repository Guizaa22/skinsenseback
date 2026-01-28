package beauty_center.modules.notes.controller;

import beauty_center.common.api.ApiResponse;
import beauty_center.modules.notes.entity.ProfessionalNote;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Professional notes REST controller (staff only)
 */
@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class ProfessionalNoteController {

    // TODO: Inject ProfessionalNoteService

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<ApiResponse<?>> getNotesByAppointment(@PathVariable UUID appointmentId) {
        // TODO: Get notes for appointment
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProfessionalNote>> createNote(@RequestBody ProfessionalNote note) {
        // TODO: Create note with authorization check
        return ResponseEntity.ok(ApiResponse.ok(null, "Note created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProfessionalNote>> updateNote(
            @PathVariable UUID id,
            @RequestBody ProfessionalNote updates) {
        // TODO: Update note
        return ResponseEntity.ok(ApiResponse.ok(null, "Note updated"));
    }

}
