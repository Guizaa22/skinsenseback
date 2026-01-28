package beauty_center.modules.clientfile.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Client file (dossier) entity containing client personal and medical information.
 * Sensitive data with strict access control required.
 */
@Entity
@Table(name = "client_file")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientFile {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id = UUID.randomUUID();

    @Column(name = "client_id", nullable = false, columnDefinition = "UUID", unique = true)
    private UUID clientId;

    @Column(name = "medical_history")
    private String medicalHistory;

    @Column(name = "allergies_and_reactions")
    private String allergiesAndReactions;

    @Column(name = "current_treatments")
    private String currentTreatments;

    @Column(name = "photo_consent_followup")
    private boolean photoConsentFollowup = false;

    @Column(name = "photo_consent_marketing")
    private boolean photoConsentMarketing = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
