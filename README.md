# TP Dev Avancée — JPA/Hibernate)




---

## 1) Architecture de l’application

L’application suit une architecture en couches :

```text
JSP (vue)
   ↑
Servlets (contrôleurs HTTP)
   ↑
Services (règles métier + transactions)
   ↑
Repositories JPA (accès données)
   ↑
Entités JPA (User / Category / Annonce)
   ↑
PostgreSQL (prod) / H2 (tests)
```

### 2.1 Couche Web (Servlets + JSP)
- Les **Servlets** reçoivent les requêtes HTTP, valident les paramètres, appellent les services et choisissent la vue JSP.
- Les **JSP** affichent les données, les messages d’erreur, et les valeurs en cas d’erreur de formulaire
- Le **AuthFilter** protège les routes `annonce-*` et `index.jsp` via la session (`userId`).

### 2.2 Couche Service
- Centralise les règles métier de l’annonce (création, édition, publication, archivage, suppression, recherche).
- Gère les **transactions JPA** (`begin/commit/rollback`) dans cette couche.
- Les Servlets ne manipulent pas de transaction directement

### 2.3 Couche Repository
- Utilise JPA/JPQL pour faire le CRUD, recherche, pagination et filtres.
  (dossier Repository)

### 2.4 Couche Modèle (entités)
- `User`, `Category`, `Annonce`, `Status`.
- Relations :
    - `Annonce` → `User` (ManyToOne)
    - `Annonce` → `Category` (ManyToOne)
    - Inverses en `OneToMany` dans `User` et `Category`
- Validation Bean Validation sur les champs

### 2.5 Configuration persistence
- **Production** : `src/main/resources/META-INF/persistence.xml` (PostgreSQL)

---

## 3) Détail des composants principaux

### 3.1 Authentification
- `LoginServlet` : login/password, création session, redirection liste.
- `LogoutServlet` : invalidation session.
- `AuthService` : requête utilisateur par username ou email + password.
- `AuthFilter` : redirige vers `/login` si non authentifié.

### 3.2 Gestion des annonces
- `AnnonceList` : pagination, recherche mot-clé, filtre catégorie/statut.
- `AnnonceAdd` : formulaire + validation serveur + création annonce.
- `AnnonceUpdate` : formulaire édition + validation serveur + mise à jour.
- `AnnonceDetail` : affichage d’une annonce avec auteur/catégorie.
- `AnnonceStatus` : publish/archive selon état.
- `AnnonceDelete` : suppression.

### 3.3 Validation & gestion des erreurs
- Validation côté serveur dans `AnnonceAdd`, `AnnonceUpdate`, `LoginServlet`.
- Messages d’erreur globaux + par champ passés aux JSP.
- Valeurs de formulaire conservées si erreur.

---

## 4) Lancer le projet

### Prérequis
- Java 11+
- Maven
- PostgreSQL (base de données que vous créez)

### Commandes utiles
```bash
mvn clean test
mvn tomcat7:run
```

- Application : `http://localhost:8080/tp_avancee`

---

## 5) Lancer les tests

```bash
mvn -q test
```


---

## 6) Organisation des tests

```text
src/test/java/
  tp_avancee_dev/tp_avancee/repository/
    - AnnonceRepositoryIntegrationTest.java
    - UserRepositoryIntegrationTest.java
    - CategoryRepositoryIntegrationTest.java

  tp_avancee_dev/tp_avancee/service/
    - AnnonceServiceTest.java
    - AnnonceServiceBusinessIntegrationTest.java

  tp_avancee_dev/tp_avancee/Servlet/
    - LoginServletTest.java
    - AnnonceAddServletTest.java

  tp_avancee_dev/tp_avancee/filter/
    - AuthFilterTest.java
```

---

## Scripts SQL

La base PostgreSQL est créée via Docker.

-- =========================
-- TABLE USERS
-- =========================

CREATE TABLE users (
id BIGSERIAL PRIMARY KEY,
username VARCHAR(64) NOT NULL UNIQUE,
email VARCHAR(128) NOT NULL UNIQUE,
password VARCHAR(255) NOT NULL,
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- TABLE CATEGORY
-- =========================

CREATE TABLE category (
id BIGSERIAL PRIMARY KEY,
label VARCHAR(64) NOT NULL
);

-- =========================
-- TABLE ANNONCE
-- =========================

CREATE TABLE annonce (
id BIGSERIAL PRIMARY KEY,
title VARCHAR(64) NOT NULL,
description VARCHAR(256) NOT NULL,
address VARCHAR(64) NOT NULL,
mail VARCHAR(64) NOT NULL,
date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
status VARCHAR(20) NOT NULL,
author_id BIGINT NOT NULL,
category_id BIGINT NOT NULL,
CONSTRAINT fk_author FOREIGN KEY (author_id) REFERENCES users(id),
CONSTRAINT fk_category FOREIGN KEY (category_id) REFERENCES category(id)
);

-- =========================
-- INSERT USERS
-- =========================

INSERT INTO users (username, email, password)
VALUES
('admin', 'admin@test.com', 'admin'),
('alice', 'alice@test.com', 'secret');

-- =========================
-- INSERT CATEGORIES
-- =========================

INSERT INTO category (label)
VALUES
('Sport'),
('Maison'),
('Informatique');

-- =========================
-- INSERT ANNONCES
-- =========================

INSERT INTO annonce (title, description, address, mail, status, author_id, category_id)
VALUES
('Velo route', 'Super velo carbone', 'Lille', 'velo@test.com', 'PUBLISHED', 2, 1),
('Canape 3 places', 'Bon etat', 'Paris', 'canape@test.com', 'DRAFT', 2, 2),
('PC portable', '16Go RAM SSD', 'Lyon', 'pc@test.com', 'ARCHIVED', 1, 3);


-- =========================
-- DROP TABLES 
-- =========================

DROP TABLE IF EXISTS annonce CASCADE;
DROP TABLE IF EXISTS category CASCADE;
DROP TABLE IF EXISTS users CASCADE;

(au cas où vous voudriez réinitialiser la base)

---

#  Problèmes rencontrés et solutions apportées


- La migration de JDBC vers JPA
- La gestion des transactions
- Les relations entre entités (Lazy Loading)
- La validation des formulaires
- La gestion des sessions et filtres


# Exercice 4 – Mauvaise gestion des transactions

## Problème

Au départ les transactions (begin, commit, rollback) étaient placées dans les classes Repository

L’architecture demandée:

- Les **Servlets** ne gèrent pas les transactions
- Les **Repositories** ne gèrent pas les transactions
- Les **Services** gèrent les transactions

Sinon :
- Mauvaise séparation des responsabilités
- Architecture non conforme


## Solution

- Suppression des transactions dans les Repository
- Passage de `EntityManager` en paramètre des méthodes Repository
- Gestion des transactions uniquement dans `AnnonceService`

---

# Problème de Lazy Loading (relations JPA)

## Problème

Lors de l’affichage du détail d’une annonce, une erreur pouvait apparaître en accédant à annonce.author.username ou annonce.category.label.


Les relations JPA sont en mode LAZY par défaut Si l’EntityManager est fermé, les données liées ne peuvent plus être chargées.

## Solution

Une méthode spécifique avec JOIN FETCH a été créée pour charger les relations (author, category) avant de fermer l’EntityManager. Cela évite les erreurs au moment de l’affichage.


---

# Erreur avec parseLong dans les Servlets

## Problème
Une méthode parseLong() appelait elle-même parseLong() ce qui provoquait une récursion infinie (StackOverflowError).

## Solution
Un appel explicite Long.parseLong(value) est utilisé avec une gestion d’erreur sécurisée


---

## Configuration du filtre d’authentification

# Problème
Une mauvaise configuration du filtre pouvait bloquer l’accès ou provoquer des redirections incorrectes et empêcher l’accès à la page de connexion ou créer une boucle de redirection

# Solution
Le filtre vérifie la présence de userId en session et redirige vers /login si nécessaire. Certaines routes sont explicitement autorisées

