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

    // Extended intake
    @Column(name = "date_of_birth")
    private String dateOfBirth;

    @Column(name = "address")
    private String address;

    @Column(name = "profession")
    private String profession;

    @Column(name = "consultation_types")
    private String consultationTypes;

    @Column(name = "how_found_us")
    private String howFoundUs;

    @Column(name = "consultation_type_autre")
    private String consultationTypeAutre;

    // Medical checkboxes (stored as individual columns)
    @Column(name = "grossesse")
    private Boolean grossesse;
    @Column(name = "diabete")
    private Boolean diabete;
    @Column(name = "maladie_auto_immune")
    private Boolean maladieAutoImmune;
    @Column(name = "troubles_hormonaux")
    private Boolean troublesHormonaux;
    @Column(name = "problems_cicatrisation")
    private Boolean problemsCicatrisation;
    @Column(name = "herpes")
    private Boolean herpes;
    @Column(name = "epilepsie")
    private Boolean epilepsie;
    @Column(name = "cancer")
    private Boolean cancer;
    @Column(name = "maladies_dermatologiques")
    private Boolean maladiesDermatologiques;
    @Column(name = "intervention_chirurgicale")
    private Boolean interventionChirurgicale;

    @Column(name = "autre_antecedent", columnDefinition = "TEXT")
    private String autreAntecedent;

    // Medications
    @Column(name = "isotretinoine")
    private Boolean isotretinoine;
    @Column(name = "corticoides")
    private Boolean corticoides;
    @Column(name = "anticoagulants")
    private Boolean anticoagulants;
    @Column(name = "hormones")
    private Boolean hormones;
    @Column(name = "antibiotiques")
    private Boolean antibiotiques;

    @Column(name = "date_arret_medicament")
    private String dateArretMedicament;

    // Allergies detail
    @Column(name = "allergies_medicamenteuses")
    private Boolean allergiesMedicamenteuses;
    @Column(name = "allergies_cosmetiques")
    private Boolean allergiesCosmetiques;
    @Column(name = "reactions_post_soins_anterieures")
    private Boolean reactionsPostSoinsAnterieures;
    @Column(name = "details_allergies", columnDefinition = "TEXT")
    private String detailsAllergies;

    // Lifestyle
    @Column(name = "tabac")
    private Boolean tabac;
    @Column(name = "exposition_solaire")
    private Boolean expositionSolaire;
    @Column(name = "cabine_uv")
    private Boolean cabineUV;
    @Column(name = "sport_intensif")
    private Boolean sportIntensif;
    @Column(name = "stress_important")
    private Boolean stressImportant;

    // Aesthetic history
    @Column(name = "hist_peeling")
    private Boolean histPeeling;
    @Column(name = "hist_laser")
    private Boolean histLaser;
    @Column(name = "hist_microneedling")
    private Boolean histMicroneedling;
    @Column(name = "hist_injections")
    private Boolean histInjections;
    @Column(name = "date_dernier_soin")
    private String dateDernierSoin;

    @Column(name = "details_injections", columnDefinition = "TEXT")
    private String detailsInjections;

    // Professional assessment (EMPLOYEE/ADMIN only)
    @Column(name = "diagnostic_cutane", columnDefinition = "TEXT")
    private String diagnosticCutane;

    @Column(name = "phototype")
    private String phototype;

    @Column(name = "indication_retenue", columnDefinition = "TEXT")
    private String indicationRetenue;

    @Column(name = "soin_realise", columnDefinition = "TEXT")
    private String soinRealise;

    @Column(name = "produits_parametres", columnDefinition = "TEXT")
    private String produitsParametres;

    @Column(name = "reactions_immediates", columnDefinition = "TEXT")
    private String reactionsImmediates;

    @Column(name = "recommandations_post_soin", columnDefinition = "TEXT")
    private String recommandationsPostSoin;

    @Column(name = "date_prochaine_rdv")
    private String dateProchaineRdv;

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
