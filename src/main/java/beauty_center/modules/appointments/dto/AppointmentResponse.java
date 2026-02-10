package beauty_center.modules.appointments.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Appointment response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {

    private UUID id;
    private UUID clientId;
    private UUID employeeId;
    private UUID serviceId;
    private OffsetDateTime startAt;
    private OffsetDateTime endAt;
    private String status;
    private String cancellationReason;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

}
