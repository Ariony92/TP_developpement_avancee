

# Ceci est un merge du TP4 vers la branche main pour faire fonctionner le CI + docker ! Le vrai TP4 se trouve sur sa branche TP4.













TP Développement Avancé #4 — Migration de l'application MasterAnnonce (JAX-RS / JAAS) vers **Spring Boot 3.5.5**.

---

## Architecture

```
Client (Postman / Swagger UI)
        ↓ HTTP JSON
RateLimitingFilter (50 req/min par IP)
        ↓
CorrelationIdFilter (X-Correlation-Id → MDC)
        ↓
JwtAuthenticationFilter (Bearer token)
        ↓
Controller (@RestController)
        ↓
Service (@Transactional + règles métier)
        ↓  ← LoggingAspect (@Around — log entrée/sortie/erreur via AOP)
Repository (Spring Data JPA + Specifications)
        ↓
Entités JPA (Annonce, User, Category)
        ↓
PostgreSQL
```

**Couches :**

| Couche | Rôle | Fichiers clés |
|--------|------|---------------|
| Controller | Endpoints REST, validation entrée | `AnnonceController`, `AuthController` |
| Service | Logique métier, autorisations, MapStruct | `AnnonceService` |
| Repository | Accès données, requêtes dynamiques | `AnnonceRepository` + `JpaSpecificationExecutor` |
| Security | JWT stateless, filtre Bearer, BCrypt | `JwtService`, `JwtAuthenticationFilter`, `SecurityConfig` |
| AOP | Logging centralisé (aucun log dans les controllers) | `LoggingAspect` |
| Filter | Correlation ID + Rate Limiting | `CorrelationIdFilter`, `RateLimitingFilter` |
| Specification | Filtres dynamiques + introspection Java | `AnnonceSpecification` |
| Mapper | Mapping DTO ↔ Entité (génération à la compilation) | `AnnonceMapper`, `UserMapper`, `CategoryMapper` |

---

## Stack technique

| Composant | Technologie |
|-----------|-------------|
| Framework | Spring Boot 3.5.5 |
| API REST | Spring MVC |
| Persistance | Spring Data JPA / Hibernate 6 / PostgreSQL 16 |
| Mapping DTO | MapStruct 1.6.3 |
| Sécurité | Spring Security + JWT (jjwt 0.12.6) + Refresh Token |
| AOP | Spring AOP + SLF4J / MDC |
| Rate Limiting | Filtre custom (token bucket in-memory, 50 req/min) |
| Documentation | SpringDoc OpenAPI 2.8.4 / Swagger UI |
| Monitoring | Spring Actuator (health, info, readiness, liveness) |
| Tests | JUnit 5, Mockito, MockMvc, Testcontainers (PostgreSQL) |
| Couverture | JaCoCo 0.8.12 |
| Docker | Multi-stage Dockerfile + Docker Compose |
| CI/CD | GitHub Actions (matrice Java 17/21, JaCoCo, Docker) |
| Orchestration | Kubernetes / Minikube (SuperBonus) |

---

## Endpoints

| Verbe | URI | Auth | Description |
|-------|-----|------|-------------|
| POST | `/api/auth/login` | Non | Login → access token + refresh token |
| POST | `/api/auth/refresh` | Non | Rafraîchir le token |
| GET | `/api/annonces` | Non | Liste paginée + filtres + tri |
| GET | `/api/annonces/{id}` | Non | Détail d'une annonce |
| POST | `/api/annonces` | JWT | Créer une annonce (DRAFT) |
| PUT | `/api/annonces/{id}` | JWT | Modifier une annonce |
| DELETE | `/api/annonces/{id}` | JWT | Supprimer (si ARCHIVED) |
| PATCH | `/api/annonces/{id}/status` | JWT | Changer le statut |
| GET | `/api/annonces/meta/filterable-fields` | Non | Champs filtrables (introspection) |
| GET | `/actuator/health` | Non | Health check |
| GET | `/swagger-ui.html` | Non | Documentation Swagger UI |

---

## Choix techniques et justifications

### Testcontainers (Option 1) vs Service container PostgreSQL (Option 2)

**Choix : Option 1 — Testcontainers.**
Les tests d'intégration démarrent automatiquement un conteneur PostgreSQL éphémère via Testcontainers (`@ServiceConnection`). Ce choix garantit que les tests tournent contre une **vraie base PostgreSQL** identique à la production, sans dépendre de la configuration du workflow CI. Le pipeline CI est plus simple (pas de bloc `services:`) et les tests sont portables : ils fonctionnent aussi bien en local qu'en CI sans aucune configuration supplémentaire.

### MapStruct vs mapping manuel

**Choix : MapStruct** (génération de code à la compilation).
Élimine tout mapping manuel (`new DTO()`, `dto.setXxx(entity.getXxx())`). Le code généré est type-safe, performant (pas de réflexion au runtime), et toute erreur de mapping est détectée à la compilation.

### Rate Limiting in-memory vs Redis

**Choix : Filtre custom in-memory** (token bucket par IP, `ConcurrentHashMap`).
Suffisant pour une API mono-instance. Pas de dépendance externe (Redis), déploiement simple. Les headers `X-RateLimit-Limit` et `X-RateLimit-Remaining` sont renvoyés à chaque réponse. Pour un déploiement multi-instances, il faudrait migrer vers Redis ou Bucket4j.

### JWT Access Token + Refresh Token

**Choix : Rotation de tokens.**
L'access token expire en 1h, le refresh token en 24h. L'endpoint `POST /api/auth/refresh` permet de renouveler les deux sans re-saisir les identifiants. Le refresh token contient un claim `type: "refresh"` pour empêcher son utilisation comme access token.

### JPA Specifications + introspection vs requêtes statiques

**Choix : Specifications dynamiques.**
Les filtres (status, categoryId, authorId, q, fromDate, toDate) sont composables via `Specification.where().and()`. La recherche textuelle (`q`) détecte automatiquement les champs String de l'entité via `getDeclaredFields()` et construit un `OR` de `LIKE`. La validation des champs de tri fonctionne aussi par réflexion, empêchant l'injection via les paramètres de tri.

---

## Problèmes rencontrés et solutions

### 1. Testcontainers — "Could not find a valid Docker environment" (Windows)

**Problème :** Les tests d'intégration échouaient en local sur Windows avec Docker Desktop 29.

**Solution :** Le problème est spécifique à certaines versions de Docker Desktop sur Windows. Vérifier `docker context use desktop-linux` et s'assurer que Docker Desktop est lancé. En dernier recours, la CI GitHub Actions (Linux) fonctionne toujours.

### 2. Mockito — "reference to delete is ambiguous"

**Problème :** `verify(repo, never()).delete(any())` ne compile pas car `any()` peut matcher `Annonce` ou `Iterable`.

**Solution :** Spécifier le type : `verify(repo, never()).delete(any(Annonce.class))`.

### 3. Pods Kubernetes "0/1 Running" pendant plusieurs minutes

**Problème :** L'application Spring Boot met ~60s à démarrer dans Minikube, dépassant le `initialDelaySeconds` de la readiness probe.

**Solution :** Augmenter le `initialDelaySeconds` ou patienter. Vérifier les logs : `kubectl logs <pod> --tail=50`.

### 4. Image Docker non trouvée dans Minikube (ImagePullBackOff)

**Problème :** L'image buildée localement n'est pas visible par Minikube.

**Solution :** Builder l'image dans le daemon Docker de Minikube :
```bash
eval $(minikube docker-env)    # Linux/Mac
& minikube docker-env --shell powershell | Invoke-Expression  # Windows
docker build -t masterannonce:1.0 .
```
Et utiliser `imagePullPolicy: Never` dans le deployment.

### 5. Port 5432 déjà utilisé avec Docker Compose

**Problème :** Un PostgreSQL local bloque le port 5432.

**Solution :** Arrêter le PostgreSQL local ou changer le port dans `docker-compose.yml` (`5433:5432`).

---

## Lancement

### Docker Compose (recommandé)

```bash
docker-compose up --build
# → http://localhost:8080
# → http://localhost:8080/swagger-ui.html
```

### Local (sans Docker)

```sql
CREATE DATABASE "MasterAnnonce";
CREATE USER tpavancee WITH PASSWORD 'tpavancee';
GRANT ALL PRIVILEGES ON DATABASE "MasterAnnonce" TO tpavancee;
```

```bash
mvn clean package
mvn spring-boot:run
```

### Kubernetes (Minikube)

```bash
minikube start --driver=docker
minikube addons enable ingress
eval $(minikube docker-env)
docker build -t masterannonce:1.0 .
kubectl apply -f k8s/
kubectl port-forward svc/masterannonce-service 9090:80
# → http://localhost:9090
```

---

## Tests

```bash
mvn clean verify
```

- **16 tests unitaires** (Mockito) : règles métier, autorisations, cas d'erreur
- **12 tests d'intégration** (MockMvc + Testcontainers) : auth, CRUD, rôles, refresh token
- **Couverture JaCoCo** : rapport dans `target/site/jacoco/`
