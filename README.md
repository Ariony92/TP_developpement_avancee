##TP1 – Version remasterisée et améliorée

Ce projet est une version remasterisée et améliorée du TP1.
Il reprend toutes les fonctionnalités demandées dans le sujet original, avec :

une meilleure structuration du code (MVC, DAO)

des validations côté serveur plus complètes

un CRUD fonctionnel (Create, Read, Update, Delete)

une base de données PostgreSQL utilisée via JDBC sur Docker

⚠️ Attention : pour que l’application fonctionne correctement, une base de données doit être créée manuellement et les paramètres de connexion doivent être adaptés.

🗄️ Configuration de la base de données
1️⃣ Créer la base de données

CREATE DATABASE Nom-BD;

2️⃣ Se connecter à la base
3️⃣ Créer la table annonce

La table doit respecter strictement la structure suivante :

CREATE TABLE annonce (
    id SERIAL PRIMARY KEY,
    title VARCHAR(64),
    description VARCHAR(256),
    adress VARCHAR(64),
    mail VARCHAR(64),
    date TIMESTAMP
);

⚙️ Configuration de la connexion JDBC

Dans le projet, la connexion à la base se fait via la classe :

ConnectionDB.java

Vous devez adapter les constantes suivantes selon votre environnement :

private static final String URL = "jdbc:postgresql://localhost:5432/MasterAnnonce";
private static final String USER = "VOTRE_USER";
private static final String PASSWD = "VOTRE_MOT_DE_PASSE";

🔁 À remplacer par :

URL → l’adresse de votre base PostgreSQL (port, nom de base)
USER → votre utilisateur PostgreSQL
PASSWD → votre mot de passe PostgreSQL
