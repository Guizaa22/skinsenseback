package beauty_center.modules.clientfile.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Client file (dossier) entity containing client personal and medical information.
 * Sensitive data with strict access control required.
 * Sections: intake, medical history, aesthetic procedure history.
 * Uses OffsetDateTime for timezone-aware timestamps.
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
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(name = "client_id", nullable = false, columnDefinition = "UUID", unique = true)
    private UUID clientId;

    // ===== Intake Section =====
    @Column(name = "how_did_you_hear_about_us")
    private String howDidYouHearAboutUs;

    @Column(name = "consultation_reason", columnDefinition = "TEXT")
    private String consultationReason;

    @Column(name = "objective", columnDefinition = "TEXT")
    private String objective;

    @Column(name = "care_type")
    private String careType;

    @Column(name = "skincare_routine", columnDefinition = "TEXT")
    private String skincareRoutine;

    @Column(name = "habits", columnDefinition = "TEXT")
    private String habits;

    // ===== Medical History Section =====
    @Column(name = "medical_background", columnDefinition = "TEXT")
    private String medicalBackground;

    @Column(name = "current_treatments", columnDefinition = "TEXT")
    private String currentTreatments;

    @Column(name = "allergies_and_reactions", columnDefinition = "TEXT")
    private String allergiesAndReactions;

    // ===== Aesthetic Procedure History Section =====
    @Column(name = "procedures", columnDefinition = "TEXT")
    private String procedures;

    // ===== Consent Section =====
    @Column(name = "photo_consent_for_followup")
    @Builder.Default
    private boolean photoConsentForFollowup = false;

    @Column(name = "photo_consent_for_marketing")
    @Builder.Default
    private boolean photoConsentForMarketing = false;

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
