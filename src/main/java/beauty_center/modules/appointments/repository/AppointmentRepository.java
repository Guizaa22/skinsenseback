package beauty_center.modules.appointments.repository;

import beauty_center.modules.appointments.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Appointment persistence
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    /**
     * Find appointments for a specific client
     */
    List<Appointment> findByClientId(UUID clientId);

    /**
     * Find appointments for a specific employee
     */
    List<Appointment> findByEmployeeId(UUID employeeId);

    /**
     * Find appointments in a time range
     */
    List<Appointment> findByStartAtBetweenAndEmployeeId(LocalDateTime start, LocalDateTime end, UUID employeeId);

}
