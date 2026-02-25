poisson

# MasterAnnonce — API REST Spring Boot

## TP Développement Avancé #4 — Migration complète vers Spring Boot

---

## Table des matières

1. [Objectif du projet](#1-objectif-du-projet)
2. [Stack technique](#2-stack-technique)
3. [Architecture du projet](#3-architecture-du-projet)
4. [Structure des fichiers](#4-structure-des-fichiers)
5. [Partie I — Migration Spring Boot (Exercices 1–3)](#5-partie-i--migration-spring-boot-exercices-13)
6. [Partie II — Sécurité JWT (Exercices 4–6)](#6-partie-ii--sécurité-jwt-exercices-46)
7. [Partie III — Tests & Qualité (Exercices 7–9)](#7-partie-iii--tests--qualité-exercices-79)
8. [Partie IV — Documentation & Observabilité (Exercices 10–11)](#8-partie-iv--documentation--observabilité-exercices-1011)
9. [Partie V — Docker & CI/CD (Exercices 12–13)](#9-partie-v--docker--cicd-exercices-1213)
10. [SuperBonus — Kubernetes / Minikube](#10-superbonus--kubernetes--minikube)
11. [Endpoints de l'API](#11-endpoints-de-lapi)
12. [Règles métier](#12-règles-métier)
13. [Problèmes rencontrés et solutions](#13-problèmes-rencontrés-et-solutions)
14. [Prérequis et lancement](#14-prérequis-et-lancement)

---

## 1. Objectif du projet

Ce projet est la migration de l'application **MasterAnnonce** (TP #3, JAX-RS / JAAS / Jersey) vers une architecture moderne basée sur **Spring Boot 3.5.5**.

L'API REST gère des annonces avec :
- Exposition HTTP JSON via **Spring MVC** (`@RestController`)
- Persistance via **Spring Data JPA / Hibernate**
- Mapping DTO ↔ entité via **MapStruct** (génération de code à la compilation)
- Recherche multi-critères via **JPA Specifications + introspection Java**
- Gestion centralisée des erreurs (`@RestControllerAdvice`)
- Sécurité **stateless** via **Spring Security + JWT**
- Logging centralisé via **Spring AOP** (aucun log dans les controllers)
- Traçabilité des requêtes via **Correlation ID** (MDC / SLF4J)
- Documentation interactive via **Swagger UI / OpenAPI**
- Monitoring via **Spring Actuator** (health, info, readiness, liveness)
- Conteneurisation via **Docker** (build multi-stage) et **Docker Compose**
- Intégration continue via **GitHub Actions** (Testcontainers, matrice Java, JaCoCo)
- Déploiement Kubernetes via **Minikube** (SuperBonus)

---

## 2. Stack technique

| Composant          | Technologie                          |
|--------------------|--------------------------------------|
| Framework          | Spring Boot 3.5.5                    |
| API REST           | Spring MVC (`@RestController`)       |
| Persistance        | Spring Data JPA / Hibernate 6        |
| Base de données    | PostgreSQL 16                        |
| Mapping DTO        | MapStruct 1.6.3                      |
| Validation         | Jakarta Bean Validation              |
| Sécurité           | Spring Security + JWT (jjwt 0.12.6)  |
| AOP / Logging      | Spring AOP + SLF4J / MDC             |
| Documentation API  | SpringDoc OpenAPI 2.8.4 / Swagger UI |
| Monitoring         | Spring Actuator                      |
| Tests unitaires    | JUnit 5 + Mockito                    |
| Tests intégration  | MockMvc + Testcontainers (PostgreSQL)|
| Couverture de code | JaCoCo 0.8.12                        |
| Build              | Maven + Spring Boot Maven Plugin     |
| Conteneurisation   | Docker (multi-stage) + Docker Compose|
| CI/CD              | GitHub Actions                       |
| Orchestration      | Kubernetes / Minikube (SuperBonus)   |
| Java               | 17 (compatible 21)                   |

---

## 3. Architecture du projet

```
Client (Postman / Swagger UI / Front)
        ↓ HTTP JSON
CorrelationIdFilter (génère X-Correlation-Id → MDC)
        ↓
JwtAuthenticationFilter (vérifie le token Bearer)
        ↓
Controller (@RestController)
        ↓
Service (@Service + @Transactional + règles métier)
        ↓  ← LoggingAspect (@Around — log entrée/sortie/erreur)
Repository (Spring Data JPA + Specifications)
        ↓
Entités JPA (User / Category / Annonce)
        ↓
PostgreSQL
```

**Principes clés :**
- Les Controllers ne contiennent **aucun log** (tout est délégué à l'aspect AOP)
- Les DTOs sont mappés exclusivement via **MapStruct** (pas de `new DTO()` dans le code métier)
- La recherche multi-critères utilise **l'introspection Java** pour détecter dynamiquement les champs String de l'entité
- La validation des champs de tri se fait par **réflexion** sur `Annonce.class.getDeclaredFields()`

---

## 4. Structure des fichiers

```
src/main/java/tp_avancee_dev/tp_avancee/
├── TpAvanceeApplication.java              ← Point d'entrée Spring Boot
├── config/
│   ├── SecurityConfig.java                ← SecurityFilterChain, stateless, JWT filter
│   └── OpenApiConfig.java                 ← Configuration Swagger UI + schéma Bearer
├── controller/
│   ├── AnnonceController.java             ← CRUD REST + PATCH status + introspection
│   └── AuthController.java                ← POST /api/auth/login (JWT)
├── service/
│   └── AnnonceService.java                ← Logique métier, conversions MapStruct
├── repository/
│   ├── AnnonceRepository.java             ← JpaRepository + JpaSpecificationExecutor
│   ├── UserRepository.java
│   └── CategoryRepository.java
├── model/
│   ├── Annonce.java                       ← Entité JPA (@Version, @CreationTimestamp)
│   ├── User.java
│   ├── Category.java
│   └── Status.java                        ← Enum DRAFT / PUBLISHED / ARCHIVED
├── dto/
│   ├── AnnonceRequestDto.java
│   ├── AnnonceResponseDto.java
│   ├── StatusPatchDto.java
│   ├── LoginRequestDto.java
│   └── LoginResponseDto.java
├── mapper/
│   ├── AnnonceMapper.java                 ← MapStruct (@Mapper)
│   ├── UserMapper.java
│   └── CategoryMapper.java
├── specification/
│   └── AnnonceSpecification.java          ← Filtres dynamiques + introspection
├── security/
│   ├── JwtService.java                    ← Génération / validation JWT (jjwt)
│   ├── JwtAuthenticationFilter.java       ← Filtre HTTP Bearer
│   ├── CustomUserDetailsService.java      ← Chargement depuis la BDD
│   └── CustomUserPrincipal.java           ← UserDetails avec userId
├── filter/
│   └── CorrelationIdFilter.java           ← X-Correlation-Id → MDC
├── aspect/
│   └── LoggingAspect.java                 ← @Around sur service..*(..)
└── exception/
    ├── GlobalExceptionHandler.java        ← @RestControllerAdvice
    ├── ApiErrorResponse.java
    ├── ResourceNotFoundException.java
    ├── BusinessConflictException.java
    ├── AccessDeniedException.java
    └── ResourceNotFoundException.java

src/test/java/tp_avancee_dev/tp_avancee/
├── service/
│   └── AnnonceServiceTest.java            ← 16 tests unitaires (Mockito)
└── integration/
    ├── ApiIntegrationTest.java            ← 10 tests d'intégration (MockMvc)
    └── TestcontainersConfig.java          ← PostgreSQL automatique via Testcontainers

k8s/                                       ← Manifests Kubernetes (SuperBonus)
├── postgres-secret.yaml
├── postgres-pvc.yaml
├── postgres-deployment.yaml
├── postgres-service.yaml
├── app-configmap.yaml
├── app-deployment.yaml
├── app-service.yaml
└── ingress.yaml

.github/workflows/ci.yml                  ← Pipeline CI GitHub Actions
Dockerfile                                 ← Build multi-stage Maven → JRE Alpine
docker-compose.yml                         ← PostgreSQL + App
```

---

## 5. Partie I — Migration Spring Boot (Exercices 1–3)

### Exercice 1 — Migration du projet Maven

Le `pom.xml` a été entièrement réécrit pour Spring Boot :

| Exigence du TP                | Implémentation                                         |
|-------------------------------|--------------------------------------------------------|
| Spring Boot Parent 3.5.5      | `spring-boot-starter-parent` version 3.5.5             |
| Spring Web                    | `spring-boot-starter-web`                              |
| Spring Data JPA               | `spring-boot-starter-data-jpa`                         |
| Spring Security               | `spring-boot-starter-security`                         |
| Spring AOP                    | `spring-boot-starter-aop`                              |
| Validation                    | `spring-boot-starter-validation`                       |
| Actuator                      | `spring-boot-starter-actuator`                         |
| PostgreSQL                    | `postgresql` (scope runtime)                           |
| Spring Boot Test              | `spring-boot-starter-test` + `spring-security-test`    |
| `application.yml`             | Connexion PostgreSQL, JPA, Actuator, JWT, logging       |

**Configuration `application.yml` :**
- Connexion PostgreSQL (`localhost:5432/MasterAnnonce`)
- JPA `ddl-auto: validate` (pas de création automatique en prod)
- Actuator : health + info exposés
- JWT : secret Base64 + expiration 1h
- Logging : pattern avec `[%X{correlationId}]` pour le Correlation ID

### Exercice 2 — Migration des endpoints REST

Tous les endpoints JAX-RS (`@Path`, `@GET`, `@POST`) ont été remplacés par les annotations Spring MVC :

| Avant (JAX-RS)       | Après (Spring MVC)           |
|----------------------|------------------------------|
| `@Path`              | `@RequestMapping`            |
| `@GET`               | `@GetMapping`                |
| `@POST`              | `@PostMapping`               |
| `@PUT`               | `@PutMapping`                |
| `@DELETE`            | `@DeleteMapping`             |
| `@PathParam`         | `@PathVariable`              |
| `@QueryParam`        | `@RequestParam`              |
| `Response.ok()`      | `ResponseEntity.ok()`        |

Le `AnnonceController` expose 7 endpoints avec pagination, tri, et filtres multi-critères.

### Exercice 3 — Migration de la couche données

| Exigence                     | Implémentation                                           |
|------------------------------|----------------------------------------------------------|
| Repositories Spring Data JPA | `AnnonceRepository extends JpaRepository, JpaSpecificationExecutor` |
| Requêtes dynamiques          | `AnnonceSpecification` avec JPA Criteria API              |
| MapStruct                    | `AnnonceMapper`, `UserMapper`, `CategoryMapper` (`@Mapper(componentModel = "spring")`) |
| Introspection                | Détection dynamique des champs String via `getDeclaredFields()` |
| Validation tri               | `validateSortFields()` empêche l'injection via les champs de tri |
| `@Version`                   | Concurrence optimiste sur l'entité `Annonce`             |
| `@CreationTimestamp`         | Date de création automatique                              |

---

## 6. Partie II — Sécurité JWT (Exercices 4–6)

### Exercice 4 — Authentification JWT

**Endpoint :** `POST /api/auth/login`

| Propriété du token | Valeur                                  |
|--------------------|-----------------------------------------|
| Signé              | HMAC-SHA256 via jjwt 0.12.6             |
| Subject            | username de l'utilisateur               |
| Claims             | `userId` (Long) + `role` (String)       |
| Expiration         | Configurable (`app.jwt.expiration-ms`)  |
| Durée par défaut   | 1 heure (3 600 000 ms)                  |

**Fichiers :**
- `security/JwtService.java` — génération, extraction, validation du token
- `controller/AuthController.java` — endpoint login
- `dto/LoginRequestDto.java` — entrée `{ username, password }`
- `dto/LoginResponseDto.java` — sortie `{ token, type, expiresIn }`

### Exercice 5 — Configuration Spring Security

**`config/SecurityConfig.java`** configure :

| Exigence                    | Implémentation                                                   |
|-----------------------------|------------------------------------------------------------------|
| SecurityFilterChain         | Bean `securityFilterChain(HttpSecurity)`                          |
| Filtre JWT personnalisé     | `JwtAuthenticationFilter` ajouté avant `UsernamePasswordAuthenticationFilter` |
| Session stateless           | `SessionCreationPolicy.STATELESS`                                |
| CSRF désactivé              | Nécessaire en API stateless (pas de cookies de session)          |
| Endpoints publics           | `/api/auth/**`, `/actuator/**`, `/swagger-ui/**`, `GET /api/annonces/**` |
| Endpoints protégés          | Tout `/api/**` nécessite un token valide                         |
| Rôles ROLE_USER, ROLE_ADMIN | Gérés via `CustomUserPrincipal` + `@PreAuthorize`                |
| BCrypt                      | `PasswordEncoder` = `BCryptPasswordEncoder`                      |

### Exercice 6 — Règles métier sécurisées + AOP + Correlation ID

**AOP — Logging centralisé (`aspect/LoggingAspect.java`) :**
- `@Around("execution(* tp_avancee_dev.tp_avancee.service..*(..))")` intercepte toutes les méthodes du package service
- Log en entrée : `[ENTER] AnnonceService.create(...)`
- Log en sortie : `[EXIT] AnnonceService.create — 42ms — result=...`
- Log en erreur : `[ERROR] AnnonceService.update — 3ms — exception=BusinessConflictException: ...`
- **Sécurité** : les arguments contenant "login", "password", "token" sont masqués (`[REDACTED]`)
- **Piège JPA lazy** : les résultats longs (>150 caractères) sont tronqués pour éviter le chargement des associations lazy

**Correlation ID (`filter/CorrelationIdFilter.java`) :**
- Filtre HTTP exécuté en premier (`@Order(HIGHEST_PRECEDENCE)`)
- Lit le header `X-Correlation-Id` (ou en génère un UUID)
- Stocke dans le MDC de SLF4J → visible dans tous les logs de la requête
- Renvoie le header `X-Correlation-Id` dans la réponse HTTP

---

## 7. Partie III — Tests & Qualité (Exercices 7–9)

### Exercice 7 — Tests unitaires Service (Mockito)

**Fichier :** `src/test/java/.../service/AnnonceServiceTest.java`

**16 tests** organisés en 4 groupes `@Nested` :

| Groupe         | Tests | Ce qui est vérifié                                                      |
|----------------|-------|-------------------------------------------------------------------------|
| `getById`      | 2     | Retour DTO ok / `ResourceNotFoundException` si absent                   |
| `create`       | 2     | Création en DRAFT / `IllegalArgumentException` si catégorie invalide   |
| `update`       | 5     | Owner ok / Non-owner → 403 / Admin bypass / PUBLISHED → 409 / Version mismatch → 409 / Not found |
| `delete`       | 3     | ARCHIVED + owner ok / Non-ARCHIVED → 409 / Non-owner → 403            |
| `changeStatus` | 4     | Admin archive ok / USER archive → 403 / Owner publish ok / Same status → 409 / ARCHIVED→PUBLISHED → 409 |

Chaque test simule le `SecurityContext` pour tester les autorisations.

### Exercice 8 — Tests d'intégration REST (MockMvc)

**Fichier :** `src/test/java/.../integration/ApiIntegrationTest.java`

**10 tests** organisés en 3 groupes `@Nested` :

| Groupe            | Tests | Ce qui est vérifié                                               |
|-------------------|-------|------------------------------------------------------------------|
| `Auth`            | 5     | Login réussi → token / Identifiants invalides → 401 / POST sans token → 401 / Token invalide → 401 / GET public ok |
| `CRUD`            | 2     | Cycle de vie complet POST→GET→PUT→PATCH→DELETE / Création + liste paginée |
| `Authorization`   | 3     | USER ne peut pas archiver → 403 / Autre USER ne peut pas modifier → 403 / PUBLISHED non modifiable → 409 |

### Exercice 9 — Base de test (Testcontainers — Option 1)

**Choix : Testcontainers** (PostgreSQL automatique dans les tests)

**Justification :** Testcontainers démarre automatiquement un conteneur PostgreSQL 16 éphémère à chaque exécution des tests. Les tests tournent contre une **vraie base PostgreSQL** (pas H2), ce qui garantit la fidélité par rapport à la production.

**Fichiers :**
- `integration/TestcontainersConfig.java` — `@TestConfiguration` avec `@ServiceConnection` (Spring Boot configure automatiquement le datasource de test)
- Dépendances : `spring-boot-testcontainers`, `testcontainers:postgresql`, `testcontainers:junit-jupiter`

**Isolation :** Chaque test est `@Transactional` → rollback automatique après chaque test.

---

## 8. Partie IV — Documentation & Observabilité (Exercices 10–11)

### Exercice 10 — Documentation OpenAPI / Swagger UI

| Exigence                   | Implémentation                                          |
|----------------------------|---------------------------------------------------------|
| SpringDoc OpenAPI          | `springdoc-openapi-starter-webmvc-ui` 2.8.4             |
| Swagger UI visible         | `http://localhost:8080/swagger-ui.html`                  |
| Spécification JSON         | `http://localhost:8080/v3/api-docs`                      |
| Exemples de requêtes       | `@ExampleObject` sur POST (création annonce) et login    |
| Documentation des erreurs  | `@ApiResponse` avec codes 400, 401, 403, 404, 409       |
| Sécurité Bearer            | Bouton "Authorize" dans Swagger UI (`OpenApiConfig.java`)|

### Exercice 11 — Actuator

| Exigence                   | Implémentation                                          |
|----------------------------|---------------------------------------------------------|
| `/actuator/health`         | Activé avec détails (DB, diskSpace, ssl, ping)           |
| `/actuator/info`           | Activé avec nom, description, version, version Java      |
| Health check PostgreSQL    | Automatique via Spring Boot (détecte le driver)          |
| Readiness probe            | `/actuator/health/readiness` (activé pour Kubernetes)    |
| Liveness probe             | `/actuator/health/liveness` (activé pour Kubernetes)     |

---

## 9. Partie V — Docker & CI/CD (Exercices 12–13)

### Exercice 12 — Dockerisation

**`Dockerfile` (build multi-stage) :**

| Stage   | Image                        | Rôle                                      |
|---------|------------------------------|-------------------------------------------|
| Build   | `maven:3-eclipse-temurin-17` | Compile le projet, génère le JAR           |
| Run     | `eclipse-temurin:17-jre-alpine` | Exécute le JAR (image légère ~200 Mo)   |

**`docker-compose.yml` :**

| Service    | Image              | Rôle                                              |
|------------|--------------------|----------------------------------------------------|
| `postgres` | `postgres:16-alpine` | BDD avec health check `pg_isready`               |
| `app`      | Build depuis `.`   | Application Spring Boot, attend que Postgres soit prêt (`depends_on: condition: service_healthy`) |

**Lancement :**
```bash
docker-compose up --build
```

L'application est accessible sur `http://localhost:8080`.

### Exercice 13 — Pipeline CI GitHub Actions

**Fichier :** `.github/workflows/ci.yml`

**Déclenchement :**
- Push sur **n'importe quelle branche**
- Pull Request vers **main**

**Job `build` :**

| Étape                    | Détail                                              |
|--------------------------|-----------------------------------------------------|
| Checkout                 | `actions/checkout@v4`                                |
| Setup Java               | `actions/setup-java@v4` (Temurin, cache Maven)       |
| Build & verify           | `mvn -B clean verify` (Testcontainers démarre PostgreSQL automatiquement) |
| Upload JAR               | Artifact `master-annonce-jar` (Java 17 uniquement)   |
| Upload JaCoCo            | Artifact `jacoco-report` (Java 17 uniquement)        |

**Option choisie : Option 1 — Testcontainers** (pas de service container PostgreSQL dans le workflow, les tests Java démarrent eux-mêmes un conteneur PostgreSQL via Docker-in-Docker).

**Bonus 1 — Matrice multi-Java :**
- Le workflow s'exécute en parallèle sur **Java 17** et **Java 21**

**Bonus 2 — Build Docker sur main :**
- Job `docker` conditionné par `github.ref == 'refs/heads/main'`
- Build l'image Docker + la sauvegarde en artifact `masterannonce-docker-image`

**Bonus 3 — JaCoCo (couverture de code) :**
- Plugin `jacoco-maven-plugin` 0.8.12 dans le `pom.xml`
- Rapport généré automatiquement pendant `mvn verify`
- Uploadé en artifact `jacoco-report` dans GitHub Actions

---

## 10. SuperBonus — Kubernetes / Minikube

### Manifests Kubernetes (`k8s/`)

| Fichier                    | Type           | Rôle                                               |
|----------------------------|----------------|------------------------------------------------------|
| `postgres-secret.yaml`     | Secret         | Credentials PostgreSQL (user, password, nom de la DB)|
| `postgres-pvc.yaml`        | PersistentVolumeClaim | Stockage persistant PostgreSQL (1 Gi)         |
| `postgres-deployment.yaml` | Deployment     | Pod PostgreSQL 16 avec readiness/liveness probes     |
| `postgres-service.yaml`    | Service (ClusterIP) | Expose PostgreSQL en interne (port 5432)       |
| `app-configmap.yaml`       | ConfigMap      | Variables Spring (datasource URL, ddl-auto, port)    |
| `app-deployment.yaml`      | Deployment     | 2 replicas de l'application avec probes Actuator     |
| `app-service.yaml`         | Service (NodePort) | Expose l'application (port 80 → 8080)           |
| `ingress.yaml`             | Ingress (nginx) | Route `masterannonce.local` vers le service         |

**Probes Kubernetes :**
- **readinessProbe** : `GET /actuator/health/readiness` (délai initial 30s, intervalle 10s)
- **livenessProbe** : `GET /actuator/health/liveness` (délai initial 45s, intervalle 15s)

**Ressources limitées :**
- Requests : 256 Mi RAM, 250m CPU
- Limits : 512 Mi RAM, 500m CPU

### Déploiement sur Minikube

```bash
# 1. Démarrer Minikube
minikube start --driver=docker

# 2. Activer l'addon Ingress
minikube addons enable ingress

# 3. Configurer Docker pour builder dans Minikube
eval $(minikube docker-env)              # Linux/Mac
& minikube docker-env --shell powershell | Invoke-Expression   # Windows

# 4. Builder l'image dans Minikube
docker build -t masterannonce:1.0 .

# 5. Déployer les manifests
kubectl apply -f k8s/

# 6. Vérifier les pods
kubectl get pods

# 7. Accéder à l'application via port-forward
kubectl port-forward svc/masterannonce-service 9090:80

# L'application est accessible sur http://localhost:9090
```

### Nettoyage

```bash
kubectl delete -f k8s/
minikube stop
```

---

## 11. Endpoints de l'API

| Verbe   | URI                                    | Auth requise | Description                              |
|---------|----------------------------------------|--------------|------------------------------------------|
| POST    | `/api/auth/login`                      | Non          | Authentification, retourne un JWT         |
| GET     | `/api/annonces`                        | Non          | Liste paginée + filtres + tri            |
| GET     | `/api/annonces/{id}`                   | Non          | Détail d'une annonce                     |
| POST    | `/api/annonces`                        | Oui (JWT)    | Création d'une annonce (DRAFT)           |
| PUT     | `/api/annonces/{id}`                   | Oui (JWT)    | Mise à jour complète                     |
| DELETE  | `/api/annonces/{id}`                   | Oui (JWT)    | Suppression (si ARCHIVED)                |
| PATCH   | `/api/annonces/{id}/status`            | Oui (JWT)    | Changement de statut                     |
| GET     | `/api/annonces/meta/filterable-fields` | Non          | Champs filtrables (introspection)        |
| GET     | `/actuator/health`                     | Non          | État de santé de l'application            |
| GET     | `/actuator/info`                       | Non          | Informations sur l'application            |
| GET     | `/swagger-ui.html`                     | Non          | Documentation interactive Swagger UI      |

### Paramètres de recherche (`GET /api/annonces`)

| Paramètre    | Type    | Description                                  |
|--------------|---------|----------------------------------------------|
| `q`          | String  | Mot-clé (LIKE sur title, description, address, mail) |
| `status`     | String  | Filtre par statut (DRAFT, PUBLISHED, ARCHIVED)|
| `categoryId` | Long    | Filtre par catégorie                         |
| `authorId`   | Long    | Filtre par auteur                            |
| `fromDate`   | Instant | Date de début (ISO 8601)                     |
| `toDate`     | Instant | Date de fin (ISO 8601)                       |
| `page`       | int     | Numéro de page (défaut : 0)                  |
| `size`       | int     | Taille de page (défaut : 10)                 |
| `sort`       | String  | Tri (ex: `date,desc`)                        |

---

## 12. Règles métier

- Seul l'auteur (ou un ADMIN) peut modifier/supprimer une annonce
- Une annonce **PUBLISHED** ne peut plus être modifiée
- Une annonce doit être **ARCHIVED** avant suppression
- Une annonce **ARCHIVED** ne peut pas repasser en **PUBLISHED**
- Seul un **ADMIN** peut archiver une annonce
- L'auteur peut publier sa propre annonce
- Gestion de concurrence optimiste via `@Version`
- Un même statut ne peut pas être appliqué deux fois de suite

---

## 13. Problèmes rencontrés et solutions

### Problème 1 — `mvn` command not found (Windows)

**Symptôme :** L'exécution de `mvn clean verify` échoue avec "commande introuvable".

**Cause :** Maven n'est pas installé globalement, le projet utilise le Maven Wrapper.

**Solution :** Utiliser `./mvnw` (Linux/Mac) ou `mvnw.cmd` (Windows) au lieu de `mvn`.

```bash
# Au lieu de :
mvn clean verify

# Utiliser :
./mvnw clean verify       # Linux/Mac
mvnw.cmd clean verify     # Windows
```

---

### Problème 2 — `JAVA_HOME` not found

**Symptôme :** Le Maven Wrapper échoue avec "JAVA_HOME is not set".

**Cause :** La variable d'environnement `JAVA_HOME` n'est pas configurée.

**Solution :**

```powershell
# Windows (PowerShell) — adapter le chemin selon votre JDK
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"

# Linux/Mac
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
```

---

### Problème 3 — Erreur Mockito "reference to delete is ambiguous"

**Symptôme :** Le test unitaire échoue à la compilation avec l'erreur :
```
reference to delete is ambiguous
  both method delete(T) in CrudRepository and method delete(Iterable) in JpaRepository match
```

**Cause :** `verify(annonceRepository, never()).delete(any())` est ambigu car `any()` peut matcher `Annonce` ou `Iterable`.

**Solution :** Spécifier le type explicitement :
```java
verify(annonceRepository, never()).delete(any(Annonce.class));
```

---

### Problème 4 — Testcontainers : "Could not find a valid Docker environment" (Windows)

**Symptôme :** Les tests d'intégration échouent en local avec l'erreur :
```
Could not find a valid Docker environment
```

**Cause :** Testcontainers ne trouve pas le socket Docker. Sur Windows, Docker Desktop peut utiliser différents contextes (`default`, `desktop-linux`).

**Solution :**
1. S'assurer que Docker Desktop est lancé
2. Vérifier le contexte Docker :
```powershell
docker context ls
docker context use desktop-linux
```
3. Si le problème persiste, définir la variable d'environnement :
```powershell
$env:DOCKER_HOST = "npipe:////./pipe/docker_engine"
```
4. En dernier recours, les tests fonctionnent toujours dans la CI (GitHub Actions sur Linux).

---

### Problème 5 — Port 5432 déjà utilisé (Docker Compose)

**Symptôme :** `docker-compose up` échoue avec "port 5432 already in use".

**Cause :** Une instance PostgreSQL locale tourne déjà sur le port 5432.

**Solution :**
```bash
# Arrêter PostgreSQL local
sudo systemctl stop postgresql     # Linux
net stop postgresql-x64-16         # Windows (adapter la version)

# Ou changer le port dans docker-compose.yml
ports:
  - "5433:5432"
```

---

### Problème 6 — Image Docker non trouvée dans Minikube

**Symptôme :** Les pods Kubernetes restent en `ImagePullBackOff` ou `ErrImagePull`.

**Cause :** L'image a été buildée dans le Docker local, pas dans le Docker de Minikube.

**Solution :** Configurer Docker pour utiliser le daemon de Minikube avant le build :
```bash
eval $(minikube docker-env)              # Linux/Mac
& minikube docker-env --shell powershell | Invoke-Expression   # Windows

docker build -t masterannonce:1.0 .
```

Et s'assurer que le deployment utilise `imagePullPolicy: Never`.

---

### Problème 7 — Pods Kubernetes pas "Ready" (readiness probe fail)

**Symptôme :** `kubectl get pods` montre `0/1 Running` pendant plusieurs minutes.

**Cause :** L'application Spring Boot met ~60 secondes à démarrer, et la readiness probe a un `initialDelaySeconds` de 30 secondes, ce qui peut être insuffisant.

**Solution :** Augmenter `initialDelaySeconds` dans le deployment, ou simplement attendre que l'application finisse de démarrer. Vérifier les logs :
```bash
kubectl logs <nom-du-pod> --tail=50
```

---

## 14. Prérequis et lancement

### Prérequis

- Java 17+
- Maven 3.8+ (ou utiliser le Maven Wrapper inclus)
- PostgreSQL 16 (pour lancement local sans Docker)
- Docker Desktop (pour Docker Compose, Testcontainers, et Kubernetes)

### Option A — Lancement local (sans Docker)

```sql
-- Créer la base PostgreSQL
CREATE DATABASE "MasterAnnonce";
CREATE USER tpavancee WITH PASSWORD 'tpavancee';
GRANT ALL PRIVILEGES ON DATABASE "MasterAnnonce" TO tpavancee;
```

```bash
mvn clean package
mvn spring-boot:run
```

L'application démarre sur `http://localhost:8080`.

### Option B — Lancement avec Docker Compose

```bash
docker-compose up --build
```

Tout est automatique : PostgreSQL + Application.
Accessible sur `http://localhost:8080`.

### Option C — Lancement sur Kubernetes (Minikube)

Voir la section [SuperBonus — Kubernetes / Minikube](#10-superbonus--kubernetes--minikube).

### Codes HTTP retournés

| Code | Signification            |
|------|--------------------------|
| 200  | Succès                   |
| 201  | Créé                     |
| 204  | Supprimé                 |
| 400  | Validation / Bad Request |
| 401  | Non authentifié          |
| 403  | Accès refusé             |
| 404  | Ressource non trouvée    |
| 409  | Conflit métier           |
| 500  | Erreur interne           |
