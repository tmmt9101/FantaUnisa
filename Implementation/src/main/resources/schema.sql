CREATE DATABASE IF NOT EXISTS fantaunisa CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE fantaunisa;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS reaction;
DROP TABLE IF EXISTS comment;
DROP TABLE IF EXISTS post;
DROP TABLE IF EXISTS formation_player;
DROP TABLE IF EXISTS formation;
DROP TABLE IF EXISTS squad;
DROP TABLE IF EXISTS statistic;
DROP TABLE IF EXISTS player;
DROP TABLE IF EXISTS user;

SET FOREIGN_KEY_CHECKS = 1;

-- Tabella UTENTI
CREATE TABLE user (
   email VARCHAR(100) PRIMARY KEY,
   username VARCHAR(50) NOT NULL UNIQUE,
   password VARCHAR(64) NOT NULL,
   nome VARCHAR(50) NOT NULL,
   cognome VARCHAR(50) NOT NULL,
   ruolo VARCHAR(20) NOT NULL
);

-- Tabella CALCIATORI
CREATE TABLE player (
   id INT PRIMARY KEY,
   nome VARCHAR(100) NOT NULL,
   squadra_seriea VARCHAR(50) NOT NULL,
   ruolo VARCHAR(1) NOT NULL
);

-- Tabella STATISTICHE
CREATE TABLE statistic (
   player_id INT,
   giornata INT,
   voto FLOAT DEFAULT 6.0,
   bonus FLOAT DEFAULT 0.0,
   malus FLOAT DEFAULT 0.0,
   PRIMARY KEY (player_id, giornata),
   FOREIGN KEY (player_id) REFERENCES player(id) ON DELETE CASCADE
);

-- Tabella ROSA (SQUAD)
CREATE TABLE squad (
   user_email VARCHAR(100),
   player_id INT,
   PRIMARY KEY (user_email, player_id),
   FOREIGN KEY (user_email) REFERENCES user(email) ON DELETE CASCADE,
   FOREIGN KEY (player_id) REFERENCES player(id) ON DELETE CASCADE
);

-- Tabella FORMAZIONE
CREATE TABLE formation (
   id INT AUTO_INCREMENT PRIMARY KEY,
   user_email VARCHAR(100) NOT NULL,
   giornata INT NOT NULL,
   modulo VARCHAR(10) NOT NULL,
   UNIQUE KEY (user_email, giornata),
   FOREIGN KEY (user_email) REFERENCES user(email) ON DELETE CASCADE
);

-- Tabella DETTAGLIO FORMAZIONE (Giocatori schierati)
CREATE TABLE formation_player (
   formation_id INT,
   player_id INT,
   tipo VARCHAR(10) NOT NULL,          -- Valori: 'TITOLARE', 'PANCHINA'
   posizione INT NOT NULL,             -- Ordine (es. 1-11 titolari, 1-7 panchina)
   PRIMARY KEY (formation_id, player_id),
   FOREIGN KEY (formation_id) REFERENCES formation(id) ON DELETE CASCADE,
   FOREIGN KEY (player_id) REFERENCES player(id) ON DELETE CASCADE
);

-- Tabella POST (Community)
CREATE TABLE post (
   id INT AUTO_INCREMENT PRIMARY KEY,
   user_email VARCHAR(100) NOT NULL,
   testo TEXT,
   data_ora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   formation_id INT,                   -- Allegato formazione
   FOREIGN KEY (user_email) REFERENCES user(email) ON DELETE CASCADE,
   FOREIGN KEY (formation_id) REFERENCES formation(id) ON DELETE SET NULL
);

-- Tabella COMMENTI
CREATE TABLE comment (
   id INT AUTO_INCREMENT PRIMARY KEY,
   post_id INT NOT NULL,
   user_email VARCHAR(100) NOT NULL,
   testo TEXT NOT NULL,
   data_ora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
   FOREIGN KEY (user_email) REFERENCES user(email) ON DELETE CASCADE
);

-- Tabella REAZIONI (Like/Dislike)
CREATE TABLE reaction (
   user_email VARCHAR(100),
   post_id INT,
   tipo VARCHAR(20) NOT NULL,
   PRIMARY KEY (user_email, post_id),
   FOREIGN KEY (user_email) REFERENCES user(email) ON DELETE CASCADE,
   FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE
);