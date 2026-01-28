package beauty_center.modules.appointments.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Appointment creation request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentCreateRequest {

    @NotNull(message = "Service ID is required")
    private UUID serviceId;

    @NotNull(message = "Employee ID is required")
    private UUID employeeId;

    @NotNull(message = "Start time is required")
    private LocalDateTime startAt;

}
