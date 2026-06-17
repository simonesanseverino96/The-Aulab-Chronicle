-- Esegue tutte le CREATE TABLE con le colonne aggiornate
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    gender VARCHAR(10) NULL,
    birth_date DATE NULL,
    reset_token VARCHAR(255) NULL,
    reset_token_expiry DATE NULL,
    created_at DATE NOT NULL DEFAULT (CURRENT_DATE)
);
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50)
);
CREATE TABLE articles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    subtitle VARCHAR(100) NOT NULL,
    body TEXT NOT NULL,
    publish_date DATE,
    is_accepted BOOLEAN DEFAULT NULL,
    view_count INT DEFAULT 0 NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT,
    category_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (category_id) REFERENCES categories(id)
);
CREATE TABLE users_roles (
    id BIGINT auto_increment PRIMARY KEY,
    user_id BIGINT,
    role_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);
CREATE TABLE images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    path VARCHAR(255) NOT NULL,
    article_id BIGINT,
    is_primary TINYINT(1) NOT NULL DEFAULT 0,
    FOREIGN KEY (article_id) REFERENCES articles(id)
);
CREATE TABLE career_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    body TEXT,
    user_id BIGINT,
    role_id BIGINT,
    is_checked BOOLEAN,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);