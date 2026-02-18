# TP Développement Avancé — Backend REST Java (JAX-RS / JPA / JAAS)

## 1) Objectif du projet

Ce projet implémente une API REST de gestion d'annonces avec :

- exposition HTTP JSON via **JAX-RS (Jersey)** ;
- persistance via **JPA / Hibernate** ;
- sécurité **stateless** avec **JAAS + Bearer token** ;
- gestion centralisée des erreurs ;
- tests unitaires et d'intégration.

L'application respecte une architecture en couches et ne dépend pas de Spring.

---

## 2) Architecture

```text
Client (Postman / front)
        ↓ HTTP JSON
API REST (JAX-RS Resources + Filter)
        ↓
Services (règles métier + transactions)
        ↓
Repositories JPA (accès DB)
        ↓
Entités JPA (User / Category / Annonce)
        ↓
PostgreSQL (runtime) / H2 (tests)
```

## 2.1 Couche API (JAX-RS)
ApiResource : endpoints de base (helloWorld, params, simulation d'erreurs, openapi).

AuthResource : endpoint /api/login.

AnnonceResource : CRUD REST + patch statut.

BearerAuthFilter : contrôle du token sur endpoints protégés (@Secured).

## 2.2 Couche Service
AnnonceService : logique métier + transactions (begin/commit/rollback).

AuthService : vérification des identifiants.

ApiTokenService : génération/validation des tokens mémoire.

## 2.3 Couche Repository
AnnonceRepository, UserRepository, CategoryRepository.

Requêtes JPA/JPQL (CRUD, pagination, recherche, filtres).

## 2.4 Sécurité
JAAS login/password : DbLoginModule.

JAAS token : BearerTokenLoginModule.

Principals : UserPrincipal, RolePrincipal.

Configuration : src/main/resources/jaas.conf.

## 2.5 Validation et erreurs
Bean Validation sur DTO (@NotBlank, @NotNull, @Email, ...).

@Valid dans les ressources REST.

Mappers d'erreurs JSON normalisés (ApiErrorResponse).

# 3) Endpoints principaux
Auth
POST /api/login : authentification, retourne un Bearer token.

API de base
GET /api/helloWorld

GET /api/params?name=...&age=...

GET /api/params/{id}

GET /api/errors/{code}

GET /api/openapi

Annonces (protégés)
GET /api/annonces

GET /api/annonces/{id}

POST /api/annonces

PUT /api/annonces/{id}

DELETE /api/annonces/{id}

PATCH /api/annonces/{id}/status

# 4) Règles métier implémentées
Seul l'auteur (ou un admin) peut modifier/supprimer une annonce.

Une annonce PUBLISHED ne peut plus être modifiée.

Une annonce doit être ARCHIVED avant suppression.

Gestion de concurrence optimiste via @Version.

# 5) Prérequis
Java 11+

Maven

PostgreSQL en local

Configurer la base côté runtime dans :

src/main/resources/META-INF/persistence.xml

# 6) Lancer le projet
6.1 Build
mvn clean package
6.2 Lancer l'API (Tomcat Maven)
mvn tomcat7:run
Base URL locale :

http://localhost:8080/tp_avancee/api

# 7) Lancer les tests
Tous les tests unitaires actifs
mvn test
Vérification Maven complète
mvn verify
Profils disponibles
mvn -Punit-tests test
mvn -Pintegration-tests verify

# 8) Organisation des tests
src/test/java/tp_avancee_dev/tp_avancee/
  - ApiErrorMappersUnitTest.java
  - DbLoginModuleTest.java
  - BearerTokenLoginModuleTest.java
  - AnnonceServiceTest.java
  - ApiRestIT.java
  - AnnonceRepositoryIntegrationTest.java
  - UserRepositoryIntegrationTest.java
  - CategoryRepositoryIntegrationTest.java
  - AnnonceServiceBusinessIntegrationTest.java

src/test/resources/
  META-INF/persistence.xml
  sql/test-dataset.sql

# 9)Problèmes rencontrés et solutions apportées
9.1 Transactions placées au mauvais niveau
Problème : les transactions étaient initialement gérées trop bas dans la couche d'accès aux données, ce qui mélangeait responsabilités techniques et métier.


Solution : transactions déplacées dans la couche Service uniquement, les repositories reçoivent un EntityManager sans ouvrir/fermer de transaction.

9.2 Erreurs Lazy Loading au moment du mapping DTO
Problème : certaines lectures sur author/category déclenchaient des erreurs après fermeture de l'EntityManager.



Solution : ajout d'une méthode repository dédiée avec JOIN FETCH pour charger explicitement les relations nécessaires au détail.

9.3 Endpoint protégé qui répondait 401 malgré token valide
Problème : le filtre lisait le header Authorization mais le contexte de sécurité n'était pas systématiquement propagé.


Solution : reconstruction complète du Subject via JAAS token + injection explicite du SecurityContext dans la requête.

9.4 Mauvais format de payload de login
Problème : des tests envoyaient login alors que l'API traitait username, ce qui créait des erreurs de validation.


Solution : alignement DTO + alias JSON (@JsonAlias("login")) pour accepter les deux notations et fiabiliser les tests.

9.5 Chaîne de tests cassée après refactor REST
Problème : des tests historiques Servlet/JSP ne correspondaient plus à l'architecture REST et empêchaient mvn test.


Solution : nettoyage des tests obsolètes, recentrage sur les tests API/Service/Sécurité, et déplacement des ressources de test dans src/test/resources.

9.6 Fichier de persistence de test non détecté
Problème : le fichier de persistence de test n'était pas au chemin Maven standard.


Solution : placement de persistence.xml dans src/test/resources/META-INF et dataset SQL dans src/test/resources/sql.

9.7 Gestion des conflits métier incomplète
Problème : certaines transitions de statut étaient trop permissives.


Solution : ajout de contrôles métier explicites dans AnnonceService avec exceptions métier mappées en HTTP 409.

# 10) Documentation API
Spécification OpenAPI : src/main/resources/openapi.yaml

Exposition brute : GET /api/openapi

Page de consultation : src/main/webapp/swagger.html

