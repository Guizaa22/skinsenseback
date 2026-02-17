package beauty_center.modules.services.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * BeautyServiceEmployee join entity for many-to-many relationship between BeautyService and Employee.
 * Represents which employees are allowed to perform a specific service.
 */
@Entity
@Table(name = "beauty_service_employee")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeautyServiceEmployee {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(name = "beauty_service_id", nullable = false, columnDefinition = "UUID")
    private UUID beautyServiceId;

    @Column(name = "employee_id", nullable = false, columnDefinition = "UUID")
    private UUID employeeId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }

}

