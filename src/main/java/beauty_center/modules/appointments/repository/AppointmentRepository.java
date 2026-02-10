package beauty_center.modules.appointments.repository;

import beauty_center.modules.appointments.entity.Appointment;
import beauty_center.modules.appointments.entity.AppointmentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Appointment persistence.
 * Provides queries for filtering, overlap detection, and availability checking.
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

    /**
     * Find appointments for an employee within a date range (using OffsetDateTime).
     * Used for checking conflicts with absences.
     */
    @Query("SELECT a FROM Appointment a " +
           "WHERE a.employeeId = :employeeId " +
           "AND a.startAt < :endAt " +
           "AND a.endAt > :startAt")
    List<Appointment> findByEmployeeIdAndDateRange(
        @Param("employeeId") UUID employeeId,
        @Param("startAt") OffsetDateTime startAt,
        @Param("endAt") OffsetDateTime endAt
    );

    /**
     * Find appointments by status
     */
    List<Appointment> findByStatus(AppointmentStatus status);

    /**
     * Find non-canceled appointments for employee within date range.
     * Used for availability calculation.
     */
    @Query("SELECT a FROM Appointment a " +
           "WHERE a.employeeId = :employeeId " +
           "AND a.status != 'CANCELED' " +
           "AND a.startAt < :endAt " +
           "AND a.endAt > :startAt " +
           "ORDER BY a.startAt")
    List<Appointment> findNonCanceledByEmployeeIdAndDateRange(
        @Param("employeeId") UUID employeeId,
        @Param("startAt") OffsetDateTime startAt,
        @Param("endAt") OffsetDateTime endAt
    );

    /**
     * Find appointments with filters (client, employee, status, date range).
     * All parameters are optional (nullable).
     */
    @Query("SELECT a FROM Appointment a " +
           "WHERE (:clientId IS NULL OR a.clientId = :clientId) " +
           "AND (:employeeId IS NULL OR a.employeeId = :employeeId) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:startAt IS NULL OR a.startAt >= :startAt) " +
           "AND (:endAt IS NULL OR a.endAt <= :endAt) " +
           "ORDER BY a.startAt DESC")
    List<Appointment> findWithFilters(
        @Param("clientId") UUID clientId,
        @Param("employeeId") UUID employeeId,
        @Param("status") AppointmentStatus status,
        @Param("startAt") OffsetDateTime startAt,
        @Param("endAt") OffsetDateTime endAt
    );

    /**
     * Check if there are overlapping appointments for the same employee (excluding canceled).
     * Used for availability validation before booking.
     * The excludeId parameter allows checking for updates (pass UUID.randomUUID() for new appointments).
     */
    @Query("SELECT COUNT(a) > 0 FROM Appointment a " +
           "WHERE a.employeeId = :employeeId " +
           "AND a.status != 'CANCELED' " +
           "AND a.id != :excludeId " +
           "AND a.startAt < :endAt " +
           "AND a.endAt > :startAt")
    boolean existsOverlappingAppointment(
        @Param("employeeId") UUID employeeId,
        @Param("startAt") OffsetDateTime startAt,
        @Param("endAt") OffsetDateTime endAt,
        @Param("excludeId") UUID excludeId
    );

    /**
     * Find overlapping appointments with pessimistic write lock.
     * This prevents concurrent bookings for the same time slot by locking the rows.
     * Used during appointment creation to prevent race conditions.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Appointment a " +
           "WHERE a.employeeId = :employeeId " +
           "AND a.status != 'CANCELED' " +
           "AND a.startAt < :endAt " +
           "AND a.endAt > :startAt")
    List<Appointment> findOverlappingAppointmentsWithLock(
        @Param("employeeId") UUID employeeId,
        @Param("startAt") OffsetDateTime startAt,
        @Param("endAt") OffsetDateTime endAt
    );

}
