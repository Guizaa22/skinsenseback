package beauty_center.modules.notes.service;

import beauty_center.common.error.BusinessRuleViolationException;
import beauty_center.common.error.EntityNotFoundException;
import beauty_center.modules.audit.service.AuditService;
import beauty_center.modules.appointments.entity.Appointment;
import beauty_center.modules.appointments.entity.AppointmentStatus;
import beauty_center.modules.appointments.repository.AppointmentRepository;
import beauty_center.modules.notes.config.NotesProperties;
import beauty_center.modules.notes.dto.ProfessionalNoteCreateRequest;
import beauty_center.modules.notes.dto.ProfessionalNoteResponse;
import beauty_center.modules.notes.dto.ProfessionalNoteUpdateRequest;
import beauty_center.modules.notes.entity.ProfessionalNote;
import beauty_center.modules.notes.repository.ProfessionalNoteRepository;
import beauty_center.modules.users.entity.Role;
import beauty_center.modules.users.entity.UserAccount;
import beauty_center.modules.users.repository.UserAccountRepository;
import beauty_center.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing professional notes on appointments.
 *
 * Business rules (7.2):
 * - Notes can only be added if appointment is COMPLETED (recommended) or at least CONFIRMED.
 * - Author must be the employee assigned to the appointment, or an admin (override).
 * - Clients cannot create or update notes.
 * - Clients can view notes only if 'notes.client-can-view' is enabled.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProfessionalNoteService {

    private final ProfessionalNoteRepository noteRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserAccountRepository userAccountRepository;
    private final CurrentUser currentUser;
    private final NotesProperties notesProperties;
    private final AuditService auditService;

    /**
     * Get notes for an appointment.
     * - Employee/Admin: always allowed.
     * - Client: only if notes.client-can-view is true AND the client owns the appointment.
     */
    @Transactional(readOnly = true)
    public List<ProfessionalNoteResponse> getNotesByAppointment(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment", appointmentId));

        UUID userId = currentUser.getUserId();
        boolean isStaff = currentUser.hasAnyRole("ADMIN", "EMPLOYEE");

        if (!isStaff) {
            // Client access
            if (!notesProperties.isClientCanView()) {
                throw new AccessDeniedException("Clients are not allowed to view professional notes");
            }
            if (!appointment.getClientId().equals(userId)) {
                throw new AccessDeniedException("You can only view notes for your own appointments");
            }
        }

        return noteRepository.findByAppointmentId(appointmentId)
                .stream()
                .map(ProfessionalNoteResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Create a professional note for an appointment.
     *
     * Integrity rules:
     * - Appointment must be COMPLETED or at least CONFIRMED (not CANCELED).
     * - Author must be the assigned employee or an admin.
     * - Clients cannot create notes.
     */
    public ProfessionalNoteResponse createNote(UUID appointmentId, ProfessionalNoteCreateRequest request) {
        UUID userId = requireStaffUser();

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment", appointmentId));

        // Validate appointment status: must be COMPLETED or CONFIRMED
        validateAppointmentStatus(appointment);

        // Validate author: must be assigned employee or admin
        validateNoteAuthor(appointment, userId);

        ProfessionalNote note = ProfessionalNote.builder()
                .id(UUID.randomUUID())
                .appointmentId(appointmentId)
                .employeeId(userId)
                .diagnostic(request.getDiagnostic())
                .phototype(request.getPhototype())
                .carePerformed(request.getCarePerformed())
                .productsAndParameters(request.getProductsAndParameters())
                .reactions(request.getReactions())
                .recommendations(request.getRecommendations())
                .nextAppointmentSuggestion(request.getNextAppointmentSuggestion())
                .build();

        ProfessionalNote saved = noteRepository.save(note);
        log.info("Professional note created: id={}, appointment={}, author={}", saved.getId(), appointmentId, userId);

        try { auditService.logCreate("ProfessionalNote", saved.getId(), saved); } catch (Exception e) { log.error("Audit log failed: {}", e.getMessage()); }

        return ProfessionalNoteResponse.fromEntity(saved);
    }

    /**
     * Update an existing professional note.
     *
     * Only the original author (or admin) can update.
     */
    public ProfessionalNoteResponse updateNote(UUID appointmentId, UUID noteId, ProfessionalNoteUpdateRequest request) {
        UUID userId = requireStaffUser();

        // Verify the appointment exists
        appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new EntityNotFoundException("Appointment", appointmentId));

        ProfessionalNote note = noteRepository.findById(noteId)
                .orElseThrow(() -> new EntityNotFoundException("ProfessionalNote", noteId));

        // Verify the note belongs to this appointment
        if (!note.getAppointmentId().equals(appointmentId)) {
            throw new BusinessRuleViolationException(
                    "Note does not belong to the specified appointment",
                    "NOTE_APPOINTMENT_MISMATCH");
        }

        // Only the original author or admin can update
        boolean isAdmin = currentUser.hasRole("ADMIN");
        if (!isAdmin && !note.getEmployeeId().equals(userId)) {
            throw new AccessDeniedException("Only the note author or an admin can update this note");
        }

        // Apply non-null updates
        if (request.getDiagnostic() != null) {
            note.setDiagnostic(request.getDiagnostic());
        }
        if (request.getPhototype() != null) {
            note.setPhototype(request.getPhototype());
        }
        if (request.getCarePerformed() != null) {
            note.setCarePerformed(request.getCarePerformed());
        }
        if (request.getProductsAndParameters() != null) {
            note.setProductsAndParameters(request.getProductsAndParameters());
        }
        if (request.getReactions() != null) {
            note.setReactions(request.getReactions());
        }
        if (request.getRecommendations() != null) {
            note.setRecommendations(request.getRecommendations());
        }
        if (request.getNextAppointmentSuggestion() != null) {
            note.setNextAppointmentSuggestion(request.getNextAppointmentSuggestion());
        }

        ProfessionalNote saved = noteRepository.save(note);
        log.info("Professional note updated: id={}, updatedBy={}", noteId, userId);

        try { auditService.logUpdate("ProfessionalNote", noteId, null, saved); } catch (Exception e) { log.error("Audit log failed: {}", e.getMessage()); }

        return ProfessionalNoteResponse.fromEntity(saved);
    }

    // ===== Private Helpers =====

    /**
     * Ensure the current user is staff (EMPLOYEE or ADMIN).
     * Clients are never allowed to create/update notes.
     *
     * @return the current user's UUID
     */
    private UUID requireStaffUser() {
        UUID userId = currentUser.getUserId();
        if (userId == null) {
            throw new AccessDeniedException("Authentication required");
        }
        if (!currentUser.hasAnyRole("ADMIN", "EMPLOYEE")) {
            throw new AccessDeniedException("Only employees or admins can manage professional notes");
        }
        return userId;
    }

    /**
     * Appointment must be COMPLETED or at least CONFIRMED (not CANCELED).
     */
    private void validateAppointmentStatus(Appointment appointment) {
        AppointmentStatus status = appointment.getStatus();
        if (status == AppointmentStatus.CANCELED) {
            throw new BusinessRuleViolationException(
                    "Cannot add notes to a canceled appointment",
                    "APPOINTMENT_CANCELED");
        }
        // CONFIRMED and COMPLETED are allowed
        if (status != AppointmentStatus.CONFIRMED && status != AppointmentStatus.COMPLETED) {
            throw new BusinessRuleViolationException(
                    "Notes can only be added when appointment is CONFIRMED or COMPLETED",
                    "INVALID_APPOINTMENT_STATUS");
        }
    }

    /**
     * The author must be the employee assigned to the appointment, or an admin (override).
     */
    private void validateNoteAuthor(Appointment appointment, UUID userId) {
        boolean isAdmin = currentUser.hasRole("ADMIN");
        if (!isAdmin && !appointment.getEmployeeId().equals(userId)) {
            throw new AccessDeniedException(
                    "Only the assigned employee or an admin can add notes to this appointment");
        }
    }
}
