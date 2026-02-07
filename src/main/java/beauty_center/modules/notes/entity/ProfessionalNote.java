package beauty_center.modules.notes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Professional note entity for post-appointment staff documentation.
 * Only staff members can create/edit these notes about clients.
 * Uses OffsetDateTime for timezone-aware timestamps.
 */
@Entity
@Table(name = "professional_note")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfessionalNote {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id = UUID.randomUUID();

    @Column(name = "appointment_id", nullable = false, columnDefinition = "UUID")
    private UUID appointmentId;

    @Column(name = "employee_id", nullable = false, columnDefinition = "UUID")
    private UUID employeeId;

    @Column(name = "diagnostic", columnDefinition = "TEXT")
    private String diagnostic;

    @Column(name = "phototype")
    private String phototype;

    @Column(name = "care_performed", columnDefinition = "TEXT")
    private String carePerformed;

    @Column(name = "products_and_parameters", columnDefinition = "TEXT")
    private String productsAndParameters;

    @Column(name = "reactions", columnDefinition = "TEXT")
    private String reactions;

    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;

    @Column(name = "next_appointment_suggestion")
    private String nextAppointmentSuggestion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}

