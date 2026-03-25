package beauty_center.modules.appointments.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for reassigning an appointment to a different employee.
 * Only ADMIN can reassign appointments.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentReassignRequest {

    @NotNull(message = "Employee ID is required")
    private UUID employeeId;

}

