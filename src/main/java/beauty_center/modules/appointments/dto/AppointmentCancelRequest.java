package beauty_center.modules.appointments.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for canceling an appointment.
 * Requires a cancellation reason for audit purposes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentCancelRequest {

    @NotBlank(message = "Cancellation reason is required")
    private String cancellationReason;

}

