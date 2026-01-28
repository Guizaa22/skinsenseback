package beauty_center.modules.notes.service;

import beauty_center.modules.notes.entity.ProfessionalNote;
import beauty_center.modules.notes.repository.ProfessionalNoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Professional note service managing staff documentation.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProfessionalNoteService {

    private final ProfessionalNoteRepository professionalNoteRepository;

    /**
     * Get notes for appointment
     */
    public List<ProfessionalNote> getNotesByAppointment(UUID appointmentId) {
        return professionalNoteRepository.findByAppointmentId(appointmentId);
    }

    /**
     * Create professional note
     */
    public ProfessionalNote createNote(ProfessionalNote note) {
        // TODO: Verify employee is authorized
        // TODO: Verify appointment exists
        return professionalNoteRepository.save(note);
    }

    /**
     * Update professional note (staff only)
     */
    public ProfessionalNote updateNote(UUID noteId, ProfessionalNote updates) {
        // TODO: Verify creator authorization
        // TODO: Update note
        return null;
    }

}
