package beauty_center.modules.appointments.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Appointment creation request DTO.
 * Client books a service without selecting an employee.
 * Client ID is determined from authenticated user context.
 * Admin can optionally specify clientId to book for another client.
 * Employee is auto-assigned by the backend based on availability.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentCreateRequest {

    private UUID clientId; // Optional: admin can book for specific client

    @NotNull(message = "Service ID is required")
    private UUID serviceId;


    @NotNull(message = "Start time is required")
    private OffsetDateTime startAt;

    private String notes;

}
