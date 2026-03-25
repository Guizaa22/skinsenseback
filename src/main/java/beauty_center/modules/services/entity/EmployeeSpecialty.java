package beauty_center.modules.services.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * EmployeeSpecialty join entity for many-to-many relationship between Employee and Specialty.
 * Represents which specialties an employee has.
 */
@Entity
@Table(name = "employee_specialty")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeSpecialty {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(name = "employee_id", nullable = false, columnDefinition = "UUID")
    private UUID employeeId;

    @Column(name = "specialty_id", nullable = false, columnDefinition = "UUID")
    private UUID specialtyId;

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

