package beauty_center.modules.notes.controller;

import beauty_center.common.api.ApiResponse;
import beauty_center.modules.notes.dto.ProfessionalNoteCreateRequest;
import beauty_center.modules.notes.dto.ProfessionalNoteResponse;
import beauty_center.modules.notes.dto.ProfessionalNoteUpdateRequest;
import beauty_center.modules.notes.service.ProfessionalNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for professional notes on appointments.
 *
 * Routes:
 * - POST /api/appointments/{id}/notes  — create note (employee/admin)
 * - GET  /api/appointments/{id}/notes  — list notes (employee/admin always; client if config flag)
 * - PUT  /api/appointments/{id}/notes/{noteId} — update note (original author or admin)
 */
@RestController
@RequestMapping("/api/appointments/{appointmentId}/notes")
@RequiredArgsConstructor
public class ProfessionalNoteController {

    private final ProfessionalNoteService noteService;

    /**
     * Get all professional notes for an appointment.
     * Employee/Admin: always allowed.
     * Client: only if 'notes.client-can-view' is enabled (checked in service).
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ProfessionalNoteResponse>>> getNotesByAppointment(
            @PathVariable UUID appointmentId) {

        List<ProfessionalNoteResponse> notes = noteService.getNotesByAppointment(appointmentId);
        return ResponseEntity.ok(ApiResponse.ok(notes, "Notes retrieved successfully"));
    }

    /**
     * Create a professional note for an appointment.
     * Only employee (assigned to appointment) or admin can create.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<ApiResponse<ProfessionalNoteResponse>> createNote(
            @PathVariable UUID appointmentId,
            @Valid @RequestBody ProfessionalNoteCreateRequest request) {

        ProfessionalNoteResponse note = noteService.createNote(appointmentId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(note, "Professional note created successfully"));
    }

    /**
     * Update a professional note.
     * Only the original author (employee) or admin can update.
     */
    @PutMapping("/{noteId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<ApiResponse<ProfessionalNoteResponse>> updateNote(
            @PathVariable UUID appointmentId,
            @PathVariable UUID noteId,
            @Valid @RequestBody ProfessionalNoteUpdateRequest request) {

        ProfessionalNoteResponse note = noteService.updateNote(appointmentId, noteId, request);
        return ResponseEntity.ok(ApiResponse.ok(note, "Professional note updated successfully"));
    }
}
