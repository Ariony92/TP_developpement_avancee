# TP AIR #1 — MasterAnnonce (version remasterisée)

## Contexte pédagogique

Ce projet correspond au **TP AIR #1** : *Introduction au développement d'applications Web en Java EE*.

**Prérequis** : HTML, CSS, Java/JEE, bases de données et SQL.

**Notions abordées** :
- Serveur d'application **Tomcat**
- Serveur de base de données **PostgreSQL**
- **Servlets** et **JSP** pour une application web
- **JDBC** pour l'accès aux données
- **Maven** pour le build et le packaging

Cette version est une **remasterisation** du TP1 : même cahier des charges, avec une meilleure structuration du code (MVC, DAO), des validations côté serveur plus complètes et un CRUD entièrement fonctionnel.

---

## Ce que fait l'application

**MasterAnnonce** est une application web de gestion d'annonces. Elle permet de :

1. **Afficher un message d'accueil** — Hello World (Servlet + JSP)
2. **Saisir un nom** — formulaire avec passage de paramètre (Hello + nom)
3. **Créer une annonce** — formulaire (title, description, adress, mail) → enregistrement en base
4. **Lister les annonces** — affichage de toutes les annonces en base
5. **Modifier une annonce** — formulaire pré-rempli, mise à jour en base (paramètre `id` dans l'URL)
6. **Supprimer une annonce** — suppression en base (paramètre `id` dans l'URL)

L'application repose sur une **architecture en couches** :
- **Vue** : JSP (`AnnonceAdd.jsp`, `AnnonceList.jsp`, `AnnonceUpdate.jsp`, `hello-form.jsp`, `index.jsp`)
- **Contrôleur** : Servlets (`AnnonceAdd`, `AnnonceList`, `AnnonceUpdate`, `AnnonceDelete`, `HelloServlet`)
- **Modèle / accès données** : classe `Annonce`, **DAO** abstrait (`DAO.java`) et implémentation (`AnnonceDAO.java`), connexion JDBC via `ConnectionDB.java`

Les Servlets gèrent le flux (doGet = affichage formulaire, doPost = traitement + redirection), valident les champs (tous obligatoires) et délèguent la persistance au DAO. La base de données utilisée est **PostgreSQL** (connexion JDBC, pas JPA).

---

## Stack technique

| Composant   | Technologie        |
|------------|--------------------|
| Build      | Maven              |
| Serveur    | Tomcat             |
| Vue        | JSP + JSTL         |
| Contrôleur | Jakarta Servlet    |
| Base       | PostgreSQL (JDBC)  |
| Packaging  | WAR                |

---

## Prérequis

- **Java 11+**
- **Maven**
- **PostgreSQL** (local ou Docker)
- **Tomcat** (pour exécution du WAR)

---

## Configuration de la base de données

### 1. Créer la base

```sql
CREATE DATABASE MasterAnnonce;
```

### 2. Créer la table `annonce`

La table doit respecter la structure suivante :

```sql
CREATE TABLE annonce (
    id SERIAL PRIMARY KEY,
    title VARCHAR(64),
    description VARCHAR(256),
    adress VARCHAR(64),
    mail VARCHAR(64),
    date TIMESTAMP
);
```

### 3. Configurer la connexion JDBC

La connexion est gérée par la classe **`ConnectionDB.java`** (singleton). Adaptez les constantes selon votre environnement :

| Constante | À remplacer par |
|-----------|------------------|
| `URL`     | `jdbc:postgresql://localhost:5432/MasterAnnonce` (adresse, port, nom de base) |
| `USER`    | Votre utilisateur PostgreSQL |
| `PASSWD`  | Votre mot de passe PostgreSQL |

Fichier concerné : `src/main/java/.../db/ConnectionDB.java`

---

## Lancer le projet

1. **Compiler et packager** :
   ```bash
   mvn clean package
   ```

2. **Déployer le WAR** sur Tomcat : le fichier généré est `target/tp_avancee.war`. Déployez-le dans le répertoire `webapps` de Tomcat ou via l’interface d’administration.

3. **Accéder à l’application** : après démarrage de Tomcat, l’URL dépend du contexte (ex. `http://localhost:8080/tp_avancee/`).

---

## Structure du projet (résumé)

```
src/main/java/.../
  Servlet/          → AnnonceAdd, AnnonceList, AnnonceUpdate, AnnonceDelete, HelloServlet
  dao/               → DAO.java (abstract), AnnonceDAO.java
  db/                → ConnectionDB.java
  model/             → Annonce.java

src/main/webapp/
  AnnonceAdd.jsp     → Formulaire de création d’annonce
  AnnonceList.jsp    → Liste des annonces
  AnnonceUpdate.jsp  → Formulaire de mise à jour
  hello-form.jsp     → Formulaire « Hello » + nom
  index.jsp          → Page d’accueil
```

---

## Attention

Pour que l’application fonctionne correctement, la base **MasterAnnonce** doit exister, la table **annonce** doit être créée et les paramètres dans **ConnectionDB.java** doivent correspondre à votre installation PostgreSQL (URL, user, mot de passe).
