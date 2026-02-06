# Configuration de Vérification Rapide

## ✅ Vérifications de Configuration

### 1. Synchronisation des Credentials
- [x] **Username synchronisé**: `beauty` dans compose.yaml ↔ `beauty` dans application.yml
- [x] **Password synchronisé**: `beauty` dans compose.yaml ↔ `beauty` dans application.yml
- [x] **Database name synchronisé**: `beauty_center` dans compose.yaml ↔ `beauty_center` dans application.yml

### 2. Configuration de Connexion
```
Protocol: PostgreSQL
Host: localhost
Port: 5432
Database: beauty_center
Username: beauty
Password: beauty
```

### 3. Tests Disponibles

#### Pour exécuter les tests de connexion:
```bash
mvn clean test -Dtest=DatabaseConnectionTest
```

#### Pour exécuter tous les tests:
```bash
mvn clean test
```

#### Pour vérifier via Maven Failsafe (tests d'intégration):
```bash
mvn clean verify
```

### 4. Logs à Observer

Au démarrage de l'application, vous devriez voir:

```
INFO  o.s.b.a.h.HibernateJpaAutoConfiguration : HibernateJpaAutoConfiguration matched
DEBUG o.h.type.descriptor.Instantiator : Selected instantiator for HibernateJpaAutoConfiguration
INFO  o.h.dialect.PostgreSQLDialect : Using dialect: org.hibernate.dialect.PostgreSQLDialect
INFO  o.f.c.i.database.base.Database : Database: PostgreSQL 17
```

### 5. État de la Base de Données

Le fichier `compose.yaml` inclut une vérification de santé:
```yaml
healthcheck:
  test: ["CMD-SHELL", "pg_isready -U beauty"]
  interval: 10s
  timeout: 5s
  retries: 5
```

### 6. Paramètres du Pool de Connexions (HikariCP)

Configuration actuelle:
- **Maximum pool size**: 10
- **Minimum idle**: 5
- **Idle timeout**: 600000ms (10 minutes, par défaut)
- **Max lifetime**: 1800000ms (30 minutes, par défaut)

Ces paramètres sont appropriés pour une application de taille moyenne.

## 🔧 Dépannage Rapide

| Problème | Solution |
|---------|----------|
| `Connection refused` | Démarrer PostgreSQL: `docker-compose up -d` |
| `FATAL: password authentication failed` | Vérifier que `beauty:beauty` est utilisé |
| `Database "beauty_center" does not exist` | Vérifier le nom de BD dans compose.yaml |
| `Port 5432 already in use` | Vérifier `docker ps` ou `lsof -i :5432` |

## 📝 Fichiers Modifiés

1. **application.yml** - Mise à jour des credentials datasource
2. **DatabaseConnectionTest.java** - Nouveau fichier de test
3. **DATABASE_CONNECTION_REPORT.md** - Rapport complet
4. **CONFIG_VERIFICATION.md** - Ce fichier

