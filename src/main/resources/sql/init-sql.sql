-- =========================================================
-- Init PostgreSQL aligné avec le modèle JPA de production
-- =========================================================

-- Nettoyage (ordre inverse des FK)
DROP TABLE IF EXISTS annonce;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS users;

-- =========================
-- TABLE users
-- =========================
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(64) NOT NULL UNIQUE,
                       email VARCHAR(128) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- TABLE category
-- =========================
CREATE TABLE category (
                          id BIGSERIAL PRIMARY KEY,
                          label VARCHAR(64) NOT NULL UNIQUE
);

-- =========================
-- TABLE annonce
-- =========================
CREATE TABLE annonce (
                         id BIGSERIAL PRIMARY KEY,
                         version BIGINT NOT NULL DEFAULT 0,
                         title VARCHAR(64) NOT NULL,
                         description VARCHAR(256) NOT NULL,
                         address VARCHAR(64) NOT NULL,
                         mail VARCHAR(64) NOT NULL,
                         date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         status VARCHAR(16) NOT NULL,
                         author_id BIGINT NOT NULL,
                         category_id BIGINT NOT NULL,
                         CONSTRAINT fk_annonce_author
                             FOREIGN KEY (author_id) REFERENCES users(id),
                         CONSTRAINT fk_annonce_category
                             FOREIGN KEY (category_id) REFERENCES category(id),
                         CONSTRAINT chk_annonce_status
                             CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);

-- =========================
-- DONNÉES INITIALES
-- =========================
INSERT INTO users (username, email, password)
VALUES
    ('admin', 'admin@test.com', 'admin'),
    ('alice', 'alice@test.com', 'secret');

INSERT INTO category (label)
VALUES
    ('Sport'),
    ('Maison'),
    ('Informatique');

INSERT INTO annonce (version, title, description, address, mail, status, author_id, category_id)
VALUES
    (0, 'Velo route', 'Super velo carbone', 'Lille', 'velo@test.com', 'PUBLISHED',
     (SELECT id FROM users WHERE username = 'alice'),
     (SELECT id FROM category WHERE label = 'Sport')),
    (0, 'Canape 3 places', 'Bon etat', 'Paris', 'canape@test.com', 'DRAFT',
     (SELECT id FROM users WHERE username = 'alice'),
     (SELECT id FROM category WHERE label = 'Maison')),
    (0, 'PC portable', '16Go RAM SSD', 'Lyon', 'pc@test.com', 'ARCHIVED',
     (SELECT id FROM users WHERE username = 'admin'),
     (SELECT id FROM category WHERE label = 'Informatique'));
