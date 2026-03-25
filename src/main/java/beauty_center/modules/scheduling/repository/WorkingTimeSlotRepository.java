package beauty_center.modules.scheduling.repository;

import beauty_center.modules.scheduling.entity.WorkingTimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for WorkingTimeSlot persistence operations.
 * Manages employee weekly schedules.
 */
@Repository
public interface WorkingTimeSlotRepository extends JpaRepository<WorkingTimeSlot, UUID> {

    /**
     * Find all working time slots for a specific employee
     */
    List<WorkingTimeSlot> findByEmployeeId(UUID employeeId);

    /**
     * Find working time slots for a specific employee and day of week
     */
    List<WorkingTimeSlot> findByEmployeeIdAndDayOfWeek(UUID employeeId, String dayOfWeek);

    /**
     * Delete all working time slots for a specific employee
     */
    void deleteByEmployeeId(UUID employeeId);

    /**
     * Check if there are overlapping time slots for the same employee on the same day
     * Overlap occurs when:
     * - new slot starts before existing slot ends AND
     * - new slot ends after existing slot starts
     */
    @Query("SELECT COUNT(w) > 0 FROM WorkingTimeSlot w " +
           "WHERE w.employeeId = :employeeId " +
           "AND w.dayOfWeek = :dayOfWeek " +
           "AND w.id != :excludeId " +
           "AND w.startTime < :endTime " +
           "AND w.endTime > :startTime")
    boolean existsOverlappingSlot(
        @Param("employeeId") UUID employeeId,
        @Param("dayOfWeek") String dayOfWeek,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime,
        @Param("excludeId") UUID excludeId
    );

}

