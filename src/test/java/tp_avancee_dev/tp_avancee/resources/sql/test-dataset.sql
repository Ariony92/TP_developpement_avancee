DELETE FROM annonce;
DELETE FROM category;
DELETE FROM users;

INSERT INTO users (username, email, password, created_at)
VALUES
    ('alice', 'alice@test.com', 'secret', DATEADD('SECOND', -40, CURRENT_TIMESTAMP)),
    ('bob', 'bob@test.com', 'secret', DATEADD('SECOND', -30, CURRENT_TIMESTAMP)),
    ('alicia', 'alicia@test.com', 'secret', DATEADD('SECOND', -20, CURRENT_TIMESTAMP)),
    ('bizuser', 'bizuser@test.com', 'secret', DATEADD('SECOND', -10, CURRENT_TIMESTAMP));

INSERT INTO category (label)
VALUES
    ('Sport'),
    ('Maison'),
    ('Sport automobile'),
    ('Business');

INSERT INTO annonce (version, title, description, address, mail, date, status, author_id, category_id)
VALUES
    (0, 'Velo route', 'Super velo carbone', 'Lille', 'velo1@test.com', DATEADD('SECOND', -50, CURRENT_TIMESTAMP), 'PUBLISHED',
     (SELECT id FROM users WHERE username = 'alice'),
     (SELECT id FROM category WHERE label = 'Sport')),
    (0, 'Velo ville', 'Velo pratique', 'Paris', 'velo2@test.com', DATEADD('SECOND', -40, CURRENT_TIMESTAMP), 'DRAFT',
     (SELECT id FROM users WHERE username = 'alice'),
     (SELECT id FROM category WHERE label = 'Sport')),
    (0, 'Canape', 'Canape 3 places', 'Lyon', 'canape@test.com', DATEADD('SECOND', -30, CURRENT_TIMESTAMP), 'ARCHIVED',
     (SELECT id FROM users WHERE username = 'bob'),
     (SELECT id FROM category WHERE label = 'Maison')),
    (0, 'Tapis velo', 'Accessoire fitness', 'Nantes', 'fitness@test.com', DATEADD('SECOND', -50, CURRENT_TIMESTAMP), 'PUBLISHED',
     (SELECT id FROM users WHERE username = 'alice'),
     (SELECT id FROM category WHERE label = 'Sport')),
    (0, 'Business draft', 'Annonce business', 'Paris', 'biz@test.com', DATEADD('SECOND', -10, CURRENT_TIMESTAMP), 'DRAFT',
     (SELECT id FROM users WHERE username = 'bizuser'),
     (SELECT id FROM category WHERE label = 'Business'));
