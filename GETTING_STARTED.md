GUIDE DE DÉMARRAGE - BEAUTY CENTER BACKEND
==========================================

PRÉ-REQUIS
──────────

✓ Docker & Docker Compose (pour PostgreSQL)
✓ Java JDK 17+
✓ Maven 3.8+
✓ Git
✓ Un éditeur (IntelliJ IDEA, VS Code, ou autre)


ÉTAPE 1: PRÉPARER L'ENVIRONNEMENT
──────────────────────────────────

1.1 Vérifier les versions installées:

    java -version              # Doit être 17+
    mvn -version               # Doit être 3.8+
    docker --version           # Doit être installé
    docker-compose --version   # Doit être installé

1.2 Cloner le projet (si nécessaire):

    git clone <repository-url>
    cd Beauty_Center


ÉTAPE 2: DÉMARRER LA BASE DE DONNÉES
──────────────────────────────────────

2.1 Démarrer PostgreSQL 17 avec Docker Compose:

    docker-compose up -d

    Cela crée:
    - Service PostgreSQL sur localhost:5432
    - Database: beauty_center
    - User: beauty / Password: beauty
    - Volume: postgres_data (persistence)

2.2 Vérifier que PostgreSQL est actif:

    docker ps

    Doit afficher: beauty_center_postgres (healthcheck: healthy)

2.3 (Optionnel) Accéder à la base de données:

    psql -h localhost -U beauty -d beauty_center

    Password: beauty


ÉTAPE 3: COMPILER LE PROJET
────────────────────────────

3.1 Compiler avec Maven:

    mvn clean compile

    Cela:
    - Télécharge les dépendances
    - Compile le code Java
    - Génère le output dans target/

3.2 (Optionnel) Lancer les tests:

    mvn test

    Note: Les tests sont stubs (à implémenter)


ÉTAPE 4: DÉMARRER L'APPLICATION
─────────────────────────────────

4.1 Option A: Avec Maven (pendant développement):

    mvn spring-boot:run

    L'app démarre sur http://localhost:8080

4.2 Option B: Avec Java (après packaging):

    mvn clean package
    java -jar target/beauty_center-0.0.1-SNAPSHOT.jar

4.3 (Optionnel) Avec variables d'environnement:

    export JWT_SECRET=my-super-secret-key-minimum-32-characters-long
    mvn spring-boot:run


ÉTAPE 5: ACCÉDER À L'API
────────────────────────

5.1 Swagger UI (API Documentation):

    http://localhost:8080/swagger-ui.html

    Ici vous pouvez:
    - Voir tous les endpoints
    - Tester les requêtes
    - Générer les tokens JWT

5.2 Health Check:

    http://localhost:8080/actuator/health

    Response: { "status": "UP" }

5.3 API Docs JSON:

    http://localhost:8080/v3/api-docs


COMMANDES UTILES
──────────────────

Arrêter PostgreSQL:
    docker-compose down

Arrêter et supprimer les données:
    docker-compose down -v

Voir les logs PostgreSQL:
    docker-compose logs postgres

Voir les logs de l'app:
    # L'app affiche les logs directement en console

Nettoyer le projet:
    mvn clean

Rebuild complet:
    mvn clean install

Créer un JAR production:
    mvn clean package -DskipTests

Lancer les tests avec logs debug:
    mvn test -X


VARIABLES D'ENVIRONNEMENT
──────────────────────────

REQUISES:
    JWT_SECRET=your-secret-key-minimum-32-characters-long

OPTIONNELLES (avec defaults):
    SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/beauty_center
    SPRING_DATASOURCE_USERNAME=beauty
    SPRING_DATASOURCE_PASSWORD=beauty
    JWT_EXP_MINUTES=60
    JWT_REFRESH_EXP_DAYS=7
    SERVER_PORT=8080

Exemple complet (.env file):
    SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/beauty_center
    SPRING_DATASOURCE_USERNAME=beauty
    SPRING_DATASOURCE_PASSWORD=beauty
    JWT_SECRET=my-super-secret-key-minimum-32-characters-long
    JWT_EXP_MINUTES=60
    JWT_REFRESH_EXP_DAYS=7
    SERVER_PORT=8080

Linux/Mac (charger avant de lancer):
    export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/beauty_center
    export JWT_SECRET=my-super-secret-key-minimum-32-characters-long
    mvn spring-boot:run

Windows PowerShell:
    $env:JWT_SECRET = "my-super-secret-key-minimum-32-characters-long"
    mvn spring-boot:run


TESTER LES ENDPOINTS
──────────────────────

1. Login (POST /auth/login):

    curl -X POST http://localhost:8080/auth/login \
      -H "Content-Type: application/json" \
      -d '{
        "email": "user@example.com",
        "password": "password123"
      }'

    Response:
    {
      "success": true,
      "message": "Login successful",
      "data": {
        "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
        "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
        "tokenType": "Bearer",
        "expiresIn": 3600
      },
      "timestamp": "2026-01-28T10:30:00"
    }

2. Utiliser le token (dans un autre endpoint):

    curl -X GET http://localhost:8080/api/users/123 \
      -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."

3. Dans Swagger UI:

    - Cliquer sur "Authorize"
    - Entrer: Bearer <votre_token>
    - Tous les endpoints protégés vont fonctionner


STRUCTURE DU PROJET POUR DÉVELOPPEMENT
──────────────────────────────────────────

src/main/java/beauty_center/
├── modules/                  ← Ajouter vos modules ici
│   ├── auth/                 ← Déjà implémenté (stub)
│   ├── users/                ← Déjà implémenté
│   ├── services/             ← Déjà implémenté
│   └── ...
├── common/                   ← Infrastructure partagée
├── config/                   ← Configuration app
└── security/                 ← JWT & Security

Pour ajouter un nouveau module:

    1. Créer dossier: modules/{feature}/
    2. Créer sous-dossiers: entity/, repository/, service/, controller/, dto/
    3. Suivre le pattern des modules existants
    4. Pas d'import circulaires


DÉPANNAGE
──────────

Problème: "Cannot connect to database"
  → Vérifier: docker-compose up -d est lancé
  → Vérifier: docker ps montre beauty_center_postgres
  → Attendre quelques secondes, healthcheck: healthy

Problème: "Port 8080 already in use"
  → Option 1: Arrêter l'app sur le port 8080
  → Option 2: Changer le port: export SERVER_PORT=8081

Problème: "Cannot resolve symbol 'Jwts'"
  → Lancer: mvn clean install
  → Attendre le téléchargement des dépendances
  → Recharger l'IDE

Problème: "JWT token validation fails"
  → Vérifier: JWT_SECRET est défini et > 32 caractères
  → Vérifier: Token format est "Authorization: Bearer <token>"
  → Vérifier: Token n'a pas expiré

Problème: Validation error sur login
  → Email doit être un email valide (format: user@domain.com)
  → Password doit être minimum 6 caractères


WORKFLOW RECOMMANDÉ POUR DEUX DÉVELOPPEURS
────────────────────────────────────────────

Développeur 1:
  - Travailler sur les modules: appointments, clientfile
  - Branch: feature/appointments, feature/clientfile
  - Pas de conflits car modules séparés

Développeur 2:
  - Travailler sur les modules: scheduling, notifications
  - Branch: feature/scheduling, feature/notifications
  - Pas de conflicts car modules séparés

Sync:
  - Commit souvent (chaque fonctionnalité finie)
  - Push vers develop
  - Pull avant de commencer nouveau travail
  - Pas de merge conflicts car packages indépendants


DOCUMENTATION DE RÉFÉRENCE
────────────────────────────

Fichiers dans le projet root:

  README.md              ← Quick start (3 commandes)
  ARCHITECTURE.md        ← Architecture détaillée
  PROJECT_FILE_TREE.txt  ← Arborescence complète
  GENERATION_COMPLETE.txt ← Résumé génération
  PROJECT_STRUCTURE.txt  ← Vue d'ensemble

Liens externes:

  Spring Boot 3.5:      https://spring.io/projects/spring-boot
  PostgreSQL 17:        https://www.postgresql.org/
  JWT (JJWT):          https://github.com/jwtk/jjwt
  Flyway:              https://flywaydb.org/
  Swagger/OpenAPI:     https://springdoc.org/
  Lombok:              https://projectlombok.org/


RÉSUMÉ (TL;DR - 3 COMMANDES)
────────────────────────────────

1. Démarrer la DB:
   docker-compose up -d

2. Compiler et lancer:
   mvn spring-boot:run

3. Accéder à Swagger:
   http://localhost:8080/swagger-ui.html

C'est tout! L'API est opérationnelle.


PROCHAINES ÉTAPES
──────────────────

1. Implémenter la logique dans les TODO comments
2. Écrire les tests unitaires et d'intégration
3. Valider sur une instance PostgreSQL locale
4. Pusher vers le repository
5. GitHub Actions CI/CD s'exécute automatiquement
6. Code review + merge vers develop/main


═══════════════════════════════════════════════════════════════════════════

Questions? Consultez ARCHITECTURE.md pour la documentation complète.

═══════════════════════════════════════════════════════════════════════════
