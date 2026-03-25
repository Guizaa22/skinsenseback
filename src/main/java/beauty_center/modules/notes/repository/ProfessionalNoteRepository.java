package beauty_center.modules.notes.repository;

import beauty_center.modules.notes.entity.ProfessionalNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for ProfessionalNote persistence
 */
@Repository
public interface ProfessionalNoteRepository extends JpaRepository<ProfessionalNote, UUID> {

    /**
     * Find all notes for a specific appointment
     */
    List<ProfessionalNote> findByAppointmentId(UUID appointmentId);

    /**
     * Find all notes created by specific employee
     */
    List<ProfessionalNote> findByEmployeeId(UUID employeeId);

}
