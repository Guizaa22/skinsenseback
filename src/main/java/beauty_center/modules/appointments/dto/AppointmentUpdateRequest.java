package beauty_center.modules.appointments.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Request DTO for updating/rescheduling an appointment.
 * Allows changing employee, service, or time slot.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentUpdateRequest {

    @NotNull(message = "Employee ID is required")
    private UUID employeeId;

    @NotNull(message = "Service ID is required")
    private UUID serviceId;

    @NotNull(message = "Start time is required")
    private OffsetDateTime startAt;

    private String notes;

}

