package beauty_center.modules.scheduling.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

/**
 * Working time slot entity defining weekly schedule for employees.
 * Represents recurring work hours for a specific day of the week.
 */
@Entity
@Table(name = "working_time_slot")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkingTimeSlot {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id = UUID.randomUUID();

    @Column(name = "employee_id", nullable = false, columnDefinition = "UUID")
    private UUID employeeId;

    @Column(name = "day_of_week", nullable = false)
    private int dayOfWeek; // 0=Monday, 6=Sunday

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

}
