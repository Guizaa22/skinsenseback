# Phase 1 - Schéma Base de Données et Conventions API

## 📋 Vue d'ensemble

Cette phase implémente le **schéma complet Phase 1** avec toutes les tables, contraintes et indexes production-ready. Elle établit également les **conventions API globales** pour la gestion des erreurs, validation et pagination.

---

## 🎯 Objectifs complétés

### ✅ 1. Schéma de base de données (Flyway-first)

**14 tables créées avec contraintes production-ready:**

| Entité | Objectif | Clé | Spécialité |
|--------|----------|-----|-----------|
| `user_account` | Polymorphe (Client, Employee, Admin) | UUID | SINGLE_TABLE inheritance |
| `specialty` | Domaines de compétence | UUID | Unique name |
| `employee_specialty` | Relation M-to-M | UUID | Join table |
| `beauty_service` | Catalogue de services | UUID | duration_min, price |
| `beauty_service_employee` | Services autorisés | UUID | Join table |
| `working_time_slot` | Planning hebdomadaire | UUID | day_of_week (MON-SUN) |
| `absence` | Congés/absences | UUID | start_at, end_at |
| `appointment` | Réservations ⭐ | UUID | **Exclusion constraint (GiST)** |
| `client_consent` | Préférences SMS | UUID | sms_opt_in |
| `client_file` | Dossier complet (intake/médical/esthétique) | UUID | Sections multiples |
| `professional_note` | Notes post-rendez-vous | UUID | appointment_id FK |
| `notification_rule` | Templates notifications | UUID | type, channel |
| `notification_message` | Tracking notifications | UUID | status: SCHEDULED/SENT/FAILED |
| `audit_entry` | Piste d'audit immutable | UUID | entity_type, action |

**Contraintes anti-chevauchement ⭐:**
```sql
EXCLUDE USING gist (
  employee_id WITH =,
  tsrange(start_at, end_at, '[]') WITH &&
) WHERE (status != 'CANCELED')
```
- ✅ Empêche les overlaps au niveau BD
- ✅ O(log n) performance avec index GiST
- ✅ Autorise les appointments CANCELED

### ✅ 2. Conventions API globales

**Gestion d'erreurs standardisée:**
- ✅ `@ControllerAdvice` GlobalExceptionHandler
- ✅ Réponses cohérentes ApiError
- ✅ Codes HTTP standard (400/401/403/404/409/500)
- ✅ Codes d'erreur métier (errorCode field)

**Validation:**
- ✅ Jakarta validation sur DTOs
- ✅ Détails de validation en array
- ✅ Exception dédiée BusinessRuleViolationException

**Pagination:**
- ✅ Spring Pageable standard
- ✅ PaginatedResponse<T> unifié
- ✅ Métadonnées: pageNumber, totalPages, hasNext, etc.

**Format datetime:**
- ✅ OffsetDateTime (timezone-aware)
- ✅ TIMESTAMPTZ en BD
- ✅ ISO-8601 en JSON: `2026-02-06T14:30:00+01:00`

---

## 📂 Fichiers clés

### Migration Flyway
```
src/main/resources/db/migration/V4__create_phase1_complete_schema.sql (330 lignes)
```
- Crée 14 tables
- FK et indexes complets
- Exclusion constraint pour appointments

### Configuration
```
src/main/resources/application.yml
```
```yaml
flyway:
  enabled: true
  locations: classpath:db/migration

jpa:
  hibernate:
    ddl-auto: validate  # Valide schéma seulement
```

### Gestion d'erreurs
```
src/main/java/beauty_center/common/error/
├── ApiError.java                          (OffsetDateTime, errorCode)
├── GlobalExceptionHandler.java            (7 handlers)
├── EntityNotFoundException.java           (404)
├── BusinessRuleViolationException.java    (409)
└── ValidationException.java               (400)
```

### DTOs API
```
src/main/java/beauty_center/common/api/
├── PaginatedResponse.java                 (Pagination)
├── ErrorResponse.java                     (Error wrapper)
└── ApiResponseWrapper.java                (Success wrapper)
```

### Entités JPA (18 fichiers)
```
src/main/java/beauty_center/modules/
├── users/entity/UserAccount.java          (OffsetDateTime)
├── services/entity/BeautyService.java     (specialtyId, duration_min)
├── services/entity/Specialty.java         (✨ NEW)
├── appointments/entity/Appointment.java   (beautyServiceId, OffsetDateTime)
├── scheduling/entity/WorkingTimeSlot.java (dayOfWeek: String)
├── scheduling/entity/Absence.java         (OffsetDateTime)
├── notes/entity/ProfessionalNote.java     (OffsetDateTime)
├── notifications/entity/NotificationRule.java      (✨ NEW)
├── notifications/entity/NotificationMessage.java   (schéma aligné)
├── clientfile/entity/ClientFile.java      (sections complètes)
├── clientfile/entity/ClientConsent.java   (✨ NEW)
└── audit/entity/AuditEntry.java           (at/created_at)
```

---

## 🚀 Utilisation

### 1. Démarrer l'application
```bash
cd D:\projects\Beauty_Center
mvn spring-boot:run
```

### 2. Vérifier que Flyway s'exécute
```log
INFO  Flyway : Validated 4 migration(s)
INFO  Flyway : Successfully applied 4 migrations to schema "public"
```

### 3. Tester l'API (ex: validation 400)
```bash
curl -X POST http://localhost:8080/api/appointments \
  -H "Content-Type: application/json" \
  -d '{"clientId": "", "startAt": "invalid"}'

# Response:
{
  "status": 400,
  "message": "Validation failed",
  "error": "VALIDATION_ERROR",
  "errorCode": "VALIDATION_FAILED",
  "timestamp": "2026-02-06T14:30:00+01:00",
  "details": ["clientId must not be blank", ...]
}
```

### 4. Tester la contrainte d'exclusion
```sql
-- Appointment 1: CONFIRMED, 10:00-11:00
-- Appointment 2: CONFIRMED, 10:30-11:30 (même employee)
-- → ERROR: no_overlapping_appointments violated

-- Appointment 3: CANCELED, 10:30-11:30 (même employee)
-- → SUCCESS: CANCELED peut se chevaucher
```

---

## 📖 Documentation

### Fichiers à lire:

1. **IMPLEMENTATION_PHASE1_SUMMARY.md**
   - Vue complète de chaque table
   - Détails des FK et indexes
   - Exemples de contraintes

2. **TESTING_GUIDE_PHASE1.md**
   - Tests complets avec cURL
   - Exemples SQL pour vérifier constraints
   - Checklist de déploiement

3. **FILES_MODIFIED_CREATED.md**
   - Liste de tous les changements
   - Dépendances entre fichiers
   - Résumé par catégorie

4. **SQL_VERIFICATION_SCRIPT.sql**
   - Scripts SQL pour vérifier le schéma
   - Tests de contrainte d'exclusion
   - Comptage des tables

---

## 🧪 Checklist de vérification

- [ ] Application démarre sans erreur
- [ ] 4 migrations Flyway exécutées avec succès
- [ ] 14 tables créées en PostgreSQL
- [ ] Contrainte `no_overlapping_appointments` active
- [ ] Test validation 400: `curl -X POST ... -d '{invalid}'`
- [ ] Test 404: `curl GET /api/appointments/invalid-id`
- [ ] Test 409: Créer 2 appointments chevauchants
- [ ] Test pagination: `curl GET /api/appointments?page=0&size=10`
- [ ] JSON datetime: Format ISO-8601 avec offset

---

## 🔧 Dépannage

### Erreur: "relation 'user_account' does not exist"
→ Vérifier `flyway.enabled: true` dans application.yml

### Erreur: "Expected column X not found"
→ Vérifier noms de colonnes dans @Column vs migration SQL

### Exclusion constraint ne fonctionne pas
→ Vérifier que `btree_gist` extension est créée:
```sql
SELECT * FROM pg_extension WHERE extname='btree_gist';
```

---

## 📊 Statistiques

- **Fichiers créés**: 15
- **Fichiers modifiés**: 15
- **Lignes de code SQL**: 330 (V4)
- **Lignes de code Java**: ~1500 (entités + exceptions + handlers)
- **Tables BD**: 14
- **Contraintes**: 1 exclusion + ~20 FK + ~30 indexes

---

## 🎯 Prochaines étapes (Phase 2)

1. **Repositories** - Spring Data JPA pour chaque entité
2. **Services métier** - BookingService, SchedulingService, NotificationService
3. **DTOs** - Request/Response pour chaque contrôleur
4. **Contrôleurs** - REST endpoints avec validation + pagination
5. **Tests d'intégration** - Vérifier constraints et logique métier

---

## 💡 Points clés

✅ **Flyway-first** - Schéma construit par migrations, validé par Hibernate

✅ **Timezone-aware** - OffsetDateTime partout, TIMESTAMPTZ en BD

✅ **Anti-chevauchement** - Contrainte PostgreSQL GiST garantit pas d'overlaps

✅ **API cohérente** - Erreurs, validation, pagination standardisées

✅ **Audit trail** - Piste d'audit immutable pour conformité

✅ **Production-ready** - Indexes, FK, constraints, logging

---

## 📞 Support

Pour toute question:
1. Consulter IMPLEMENTATION_PHASE1_SUMMARY.md
2. Exécuter SQL_VERIFICATION_SCRIPT.sql
3. Suivre TESTING_GUIDE_PHASE1.md

