package beauty_center.modules.scheduling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Employee response DTO.
 * Returns employee information with specialties.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {

    private UUID id;
    private String fullName;
    private String email;
    private String phone;
    private boolean isActive;
    private String role;
    private List<SpecialtyDto> specialties;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecialtyDto {
        private UUID id;
        private String name;
        private String description;
    }

}

