poisson

# MasterAnnonce — Spring Boot

## TP Développement Avancé #4 — Migration vers Spring Boot

## 1) Objectif du projet

Ce projet est la migration de l'application MasterAnnonce (TP#3 JAX-RS / JAAS) vers une architecture moderne basée sur **Spring Boot 3.5.5**.

L'API REST gère des annonces avec :

- exposition HTTP JSON via **Spring MVC** (`@RestController`) ;
- persistance via **Spring Data JPA / Hibernate** ;
- mapping DTO/entité via **MapStruct** (génération de code, pas de mapping manuel) ;
- recherche multi-critères via **JPA Specifications + introspection** ;
- gestion centralisée des erreurs (`@RestControllerAdvice`) ;
- sécurité **stateless** via **Spring Security** (JWT préparé en Partie II) ;
- pagination, tri, et validation des champs de tri par réflexion.

---

## 2) Architecture

```text
Client (Postman / front)
        ↓ HTTP JSON
Controller (@RestController)
        ↓
Service (règles métier + @Transactional)
        ↓
Repository (Spring Data JPA)
        ↓
Entités JPA (User / Category / Annonce)
        ↓
PostgreSQL
```

### Couche Controller
- `AnnonceController` : CRUD REST + PATCH statut + endpoint d'introspection `/meta/filterable-fields`

### Couche Service
- `AnnonceService` : logique métier, conversions via MapStruct, règles de transition de statut

### Couche Repository
- `AnnonceRepository` (+ `JpaSpecificationExecutor`), `UserRepository`, `CategoryRepository`
- Requêtes dynamiques via `Specification`

### Mappers (MapStruct)
- `AnnonceMapper`, `UserMapper`, `CategoryMapper`
- Aucun `new DTO()` dans les controllers/services

### Specifications + Introspection
- `AnnonceSpecification` : filtres dynamiques (status, categoryId, authorId, q, fromDate, toDate)
- Recherche LIKE automatique sur tous les champs String détectés par réflexion
- Validation des champs de tri via `Annonce.class.getDeclaredFields()`

---

## 3) Endpoints

| Verbe   | URI                                  | Description                              |
|---------|--------------------------------------|------------------------------------------|
| GET     | `/api/annonces`                      | Liste paginée + filtres + tri            |
| GET     | `/api/annonces/{id}`                 | Détail d'une annonce                     |
| POST    | `/api/annonces`                      | Création                                 |
| PUT     | `/api/annonces/{id}`                 | Mise à jour complète                     |
| DELETE  | `/api/annonces/{id}`                 | Suppression (si ARCHIVED)                |
| PATCH   | `/api/annonces/{id}/status`          | Changement de statut                     |
| GET     | `/api/annonces/meta/filterable-fields` | Champs filtrables (introspection)      |

### Paramètres de recherche (GET /api/annonces)

| Paramètre   | Type    | Description                                  |
|--------------|---------|----------------------------------------------|
| `q`          | String  | Mot-clé (LIKE sur title, description, etc.)  |
| `status`     | String  | Filtre par statut (DRAFT, PUBLISHED, ARCHIVED) |
| `categoryId` | Long    | Filtre par catégorie                         |
| `authorId`   | Long    | Filtre par auteur                            |
| `fromDate`   | Instant | Date de début (ISO 8601)                     |
| `toDate`     | Instant | Date de fin (ISO 8601)                       |
| `page`       | int     | Numéro de page (défaut : 0)                  |
| `size`       | int     | Taille de page (défaut : 10)                 |
| `sort`       | String  | Tri (ex: `date,desc`)                        |

---

## 4) Règles métier

- Seul l'auteur (ou un admin) peut modifier/supprimer une annonce
- Une annonce **PUBLISHED** ne peut plus être modifiée
- Une annonce doit être **ARCHIVED** avant suppression
- Une annonce **ARCHIVED** ne peut pas repasser en **PUBLISHED**
- Gestion de concurrence optimiste via `@Version`

---

## 5) Stack technique

| Composant        | Technologie                    |
|------------------|--------------------------------|
| Framework        | Spring Boot 3.5.5              |
| API REST         | Spring MVC                     |
| Persistance      | Spring Data JPA / Hibernate    |
| Base de données  | PostgreSQL                     |
| Mapping DTO      | MapStruct 1.6.3                |
| Validation       | Bean Validation (Jakarta)      |
| Sécurité         | Spring Security (stateless)    |
| AOP              | Spring AOP                     |
| Monitoring       | Spring Actuator                |
| Tests            | JUnit 5 / Mockito / H2         |
| Build            | Maven                          |
| Java             | 17                             |

---

## 6) Prérequis

- Java 17+
- Maven 3.8+
- PostgreSQL en local

### Configuration de la base

```sql
CREATE DATABASE "MasterAnnonce";
CREATE USER tpavancee WITH PASSWORD 'tpavancee';
GRANT ALL PRIVILEGES ON DATABASE "MasterAnnonce" TO tpavancee;
```

---

## 7) Lancer le projet

```bash
mvn clean package
mvn spring-boot:run
```

L'application démarre sur **http://localhost:8080**

---

## 8) Codes HTTP

| Code | Signification           |
|------|-------------------------|
| 200  | Succès                  |
| 201  | Créé                    |
| 204  | Supprimé                |
| 400  | Validation / Bad Request|
| 404  | Ressource non trouvée   |
| 409  | Conflit métier          |
| 500  | Erreur interne          |
