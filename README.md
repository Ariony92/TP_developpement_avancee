

# ⚠️ Veuillez vous connecter à une base de donnée afin de faire fonctionner le projet !

# URL pour tester l'API

Une fois le projet lancé :

http://localhost:8080/dev_avancee_war/api


Endpoint de login :

POST http://localhost:8080/dev_avancee_war/api/login



## TP Développement Avancé — Backend REST Java (JAX-RS / JPA / JAAS)

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


### Initialiser PostgreSQL rapidement

Un script SQL prêt pour la prod est fourni ici :

- `src/main/resources/sql/init-sql.sql`

⚠️ La DB et les credentials doivent correspondre à persistence.xml pour que ça marche (adapter selon votre configuration)

Exemple d'import (c'est un exemple, adapter selon votre configuration) :
```bash
psql -U tpavancee -d MasterAnnonce -f src/main/resources/sql/init-sql.sql
```


# 6) Lancer le projet
6.1 Build
mvn clean package
6.2 Lancer l'API (Tomcat Maven)
mvn tomcat7:run
Base URL locale :

http://localhost:8080/dev_avancee_war/api


# 7) Lancer les tests

Tous les tests unitaires :
```bash
mvn test
```

Vérification Maven complète (unitaires + intégration) :
```bash
mvn verify
```

Profils disponibles :
```bash
mvn -Punit-tests test
mvn -Pintegration-tests verify
```

### Pourquoi séparer tests unitaires et tests d'intégration ?

La séparation des tests unitaires (UT) et des tests d'intégration (IT) est une bonne pratique pour plusieurs raisons :

- **Rapidité du feedback** : les tests unitaires s'exécutent en quelques secondes sans infrastructure externe (pas de BDD, pas de serveur). Le développeur obtient un retour immédiat sur la logique métier à chaque modification.
- **Indépendance de l'infrastructure** : les tests unitaires utilisent des mocks (Mockito) et ne nécessitent ni base de données ni serveur applicatif. Ils peuvent être lancés n'importe où, y compris sur un poste sans PostgreSQL installé.
- **Stabilité du pipeline CI/CD** : en séparant les exécutions, on peut lancer les UT à chaque commit (rapides, fiables) et réserver les IT (plus lents, dépendants de H2 ou d'un serveur) pour les étapes de validation avant déploiement.
- **Isolation des erreurs** : si un test unitaire échoue, on sait que c'est un problème de logique métier. Si un test d'intégration échoue, le problème peut venir de la configuration JPA, du mapping SQL, ou de la couche REST.

Dans ce projet, Maven Surefire exécute les fichiers `*Test.java` (unitaires) et Maven Failsafe exécute les fichiers `*IntegrationTest.java` et `*IT.java` (intégration), ce qui permet de les lancer indépendamment via les profils `unit-tests` et `integration-tests`.

# 8) Organisation des tests

### Tests unitaires (`mvn test`)
| Fichier | Ce qu'il teste |
|---|---|
| `AnnonceServiceTest.java` | Logique métier : changement de statut, permissions auteur/admin |
| `ApiErrorMappersUnitTest.java` | Mappers d'erreurs : 401, 409, 400 (validation) |
| `DbLoginModuleTest.java` | JAAS login/password : credentials valides, admin, échec |
| `BearerTokenLoginModuleTest.java` | JAAS token : token valide, invalide, expiré |

### Tests d'intégration (`mvn -Pintegration-tests verify`)
| Fichier | Ce qu'il teste |
|---|---|
| `ApiRestIT.java` | API REST complète : login, 401, 400, 404, token, SecurityContext |
| `AnnonceRepositoryIntegrationTest.java` | CRUD + pagination + recherche sur Annonce (H2) |
| `UserRepositoryIntegrationTest.java` | CRUD + recherche sur User (H2) |
| `CategoryRepositoryIntegrationTest.java` | CRUD + recherche sur Category (H2) |
| `AnnonceServiceBusinessIntegrationTest.java` | Flow métier complet : create → publish → search (H2) |
| `LoadTestIT.java` | Tests de charge : requêtes concurrentes (GET, login, 401) |

### Ressources de test
```
src/test/resources/
  META-INF/persistence.xml   ← persistence unit H2 (mode PostgreSQL)
  sql/test-dataset.sql       ← jeu de données (4 users, 4 catégories, 5 annonces)
```


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


# 11) Collection Postman

La collection Postman est disponible à la racine du projet : `postman_collection.json`

**Import** : File → Import → sélectionner le fichier

**Variable** : `baseUrl` = `http://localhost:8080/dev_avancee_war`

### Requêtes incluses (20 requêtes)

| Dossier | Requêtes |
|---|---|
| **API de base** | helloWorld, params (query + path), erreurs simulées (400, 404, 409, 500), openapi |
| **Authentification** | login succès, login échec (401) |
| **Sécurité** | GET sans token (401), GET token invalide (401) |
| **CRUD Annonces** | liste paginée, recherche keyword, création, création invalide (400), détail, détail 404, mise à jour (PUT) |
| **Règles métier** | publish, PUT sur PUBLISHED (409), DELETE sans archive (409), archive, DELETE après archive (204) |

**Workflow recommandé** :
1. Exécuter **login** → le token est stocké automatiquement
2. Exécuter **création** → l'id et la version sont stockés
3. Exécuter les tests dans l'ordre du dossier "Règles métier" pour vérifier le cycle complet



# 12) Scripts SQL

Scripts présents :

src/test/resources/sql/test-dataset.sql

Ils permettent de créer les données de test.

## 13) Flow d’authentification

1. L’utilisateur appelle POST /api/login avec username/password

2. AuthResource appelle JAAS via LoginContext

3. DbLoginModule vérifie les credentials en base

4. Si succès :
   → création d’un Subject
   → génération d’un token

5. Le token est retourné au client

6. Pour chaque requête protégée :

   client → Authorization: Bearer token

7. BearerAuthFilter :

   → appelle JAAS TokenLoginModule

8. TokenLoginModule :

   → valide le token
   → reconstruit le Subject

9. Le Service récupère l’utilisateur courant

→ et applique les règles métier


## Choix techniques

**Jersey** :
→ implémentation officielle JAX-RS, bien documentée, intégration native avec Tomcat via le plugin Maven.

**JAAS** :
→ solution standard Java SE pour l'authentification. Permet une séparation claire entre la logique d'authentification (LoginModule) et le transport (filtre REST). L'approche est stateless : chaque requête reconstruit le Subject via le token, sans session HTTP.

**H2 (tests)** :
→ base de données embarquée en mémoire, rapide, aucun serveur à installer. Le mode `MODE=PostgreSQL` assure la compatibilité SQL avec la base de production.

**Mapping DTO manuel (sans Builder)** :
Le TP mentionne l'opportunité d'implémenter le Pattern Builder pour le mapping Entity ↔ DTO. Nous avons choisi le **mapping manuel avec setters** dans `AnnonceMapper` pour les raisons suivantes :
- Les DTO ont peu de champs (< 12), le Builder n'apporterait pas de gain significatif en lisibilité.
- Le mapping est centralisé dans une seule classe (`AnnonceMapper`), ce qui le rend facile à maintenir.
- Le Builder est plus pertinent quand l'objet a des paramètres optionnels nombreux ou des invariants à respecter à la construction. Ici, les DTO sont de simples conteneurs de données sans logique.
- Éviter une complexité supplémentaire (classe Builder imbriquée) pour un bénéfice marginal.

## Gestion des codes HTTP

| Code | Usage |
|---|---|
| 200 | Succès (GET, PUT, PATCH) |
| 201 | Création réussie (POST) |
| 204 | Suppression réussie (DELETE) |
| 400 | Erreur de validation (Bean Validation, payload invalide) |
| 401 | Non authentifié (token absent ou invalide) |
| 403 | Accès refusé (pas l'auteur de l'annonce) |
| 404 | Ressource non trouvée |
| 409 | Conflit métier (PUBLISHED non modifiable, archivage requis) |
| 500 | Erreur interne |
