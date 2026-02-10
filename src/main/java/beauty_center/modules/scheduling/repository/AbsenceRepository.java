package beauty_center.modules.scheduling.repository;

import beauty_center.modules.scheduling.entity.Absence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Absence persistence operations.
 * Manages employee time off and vacations.
 */
@Repository
public interface AbsenceRepository extends JpaRepository<Absence, UUID> {

    /**
     * Find all absences for a specific employee
     */
    List<Absence> findByEmployeeId(UUID employeeId);

    /**
     * Find absences for an employee within a date range
     */
    @Query("SELECT a FROM Absence a " +
           "WHERE a.employeeId = :employeeId " +
           "AND a.startAt < :endAt " +
           "AND a.endAt > :startAt")
    List<Absence> findByEmployeeIdAndDateRange(
        @Param("employeeId") UUID employeeId,
        @Param("startAt") OffsetDateTime startAt,
        @Param("endAt") OffsetDateTime endAt
    );

    /**
     * Check if there are overlapping absences for the same employee
     * Overlap occurs when:
     * - new absence starts before existing absence ends AND
     * - new absence ends after existing absence starts
     */
    @Query("SELECT COUNT(a) > 0 FROM Absence a " +
           "WHERE a.employeeId = :employeeId " +
           "AND a.id != :excludeId " +
           "AND a.startAt < :endAt " +
           "AND a.endAt > :startAt")
    boolean existsOverlappingAbsence(
        @Param("employeeId") UUID employeeId,
        @Param("startAt") OffsetDateTime startAt,
        @Param("endAt") OffsetDateTime endAt,
        @Param("excludeId") UUID excludeId
    );

}

