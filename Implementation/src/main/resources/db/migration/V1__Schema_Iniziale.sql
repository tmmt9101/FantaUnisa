-- Tabella Utente
CREATE TABLE Utente (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    ruolo ENUM('Fantallenatore', 'Gestore') NOT NULL DEFAULT 'Fantallenatore',
    punteggio_reputazione INT DEFAULT 0 -- Punteggio basato sui voti ricevuti ai commenti
);

-- Tabella Calciatore (Listone)
CREATE TABLE Calciatore (
    id INT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    squadra VARCHAR(50) NOT NULL,
    ruolo ENUM('P', 'D', 'C', 'A') NOT NULL,
    quotazione INT DEFAULT 1,
    INDEX idx_nome (nome),
    INDEX idx_ruolo (ruolo)
);

-- Tabella Squadra (La rosa dell'utente)
CREATE TABLE Squadra (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_utente INT NOT NULL,
    nome_squadra VARCHAR(50) NOT NULL,
    FOREIGN KEY (id_utente) REFERENCES Utente(id) ON DELETE CASCADE
);

-- Tabella di relazione Squadra-Calciatore
CREATE TABLE Squadra_Calciatore (
    id_squadra INT NOT NULL,
    id_calciatore INT NOT NULL,
    PRIMARY KEY (id_squadra, id_calciatore),
    FOREIGN KEY (id_squadra) REFERENCES Squadra(id) ON DELETE CASCADE,
    FOREIGN KEY (id_calciatore) REFERENCES Calciatore(id) ON DELETE CASCADE
);

-- Tabella Formazione
CREATE TABLE Formazione (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_squadra INT NOT NULL,
    giornata INT NOT NULL,
    modulo VARCHAR(10) NOT NULL,
    totale_punti DECIMAL(5, 2) DEFAULT 0,
    FOREIGN KEY (id_squadra) REFERENCES Squadra(id) ON DELETE CASCADE
);

-- Tabella di relazione Formazione-Calciatore
CREATE TABLE Formazione_Calciatore (
    id_formazione INT NOT NULL,
    id_calciatore INT NOT NULL,
    titolare BOOLEAN DEFAULT TRUE,
    PRIMARY KEY (id_formazione, id_calciatore),
    FOREIGN KEY (id_formazione) REFERENCES Formazione(id) ON DELETE CASCADE,
    FOREIGN KEY (id_calciatore) REFERENCES Calciatore(id) ON DELETE CASCADE
);

-- Tabella Statistica
CREATE TABLE Statistica (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_calciatore INT NOT NULL,
    giornata INT NOT NULL,
    partite_voto INT DEFAULT 0,
    media_voto DECIMAL(4, 2),
    fanta_media DECIMAL(4, 2),
    gol_fatti INT DEFAULT 0,
    gol_subiti INT DEFAULT 0,
    rigori_parati INT DEFAULT 0,
    rigori_calciati INT DEFAULT 0,
    rigori_segnati INT DEFAULT 0,
    rigori_sbagliati INT DEFAULT 0,
    assist INT DEFAULT 0,
    ammonizioni INT DEFAULT 0,
    espulsioni INT DEFAULT 0,
    autogol INT DEFAULT 0,
    FOREIGN KEY (id_calciatore) REFERENCES Calciatore(id) ON DELETE CASCADE
);

-- pesi algoritmo
CREATE TABLE ConfigurazioneAlgoritmo (
    chiave VARCHAR(50) PRIMARY KEY,
    valore DECIMAL(4, 2) NOT NULL,
    descrizione VARCHAR(255)
);

-- inserimento pesi di default all'inizio
INSERT INTO ConfigurazioneAlgoritmo (chiave, valore, descrizione) VALUES
('P_PESO_MV', 0.60, 'Peso Media Voto Portiere'),
('P_PESO_GS', 0.30, 'Peso Gol Subiti Portiere'),
('P_PESO_COSTANZA', 0.10, 'Peso Costanza Portiere'),
('M_PESO_FM', 0.70, 'Peso Fantamedia Giocatori Movimento'),
('M_PESO_COSTANZA', 0.10, 'Peso Costanza Giocatori Movimento'),
('M_PESO_GOL', 0.15, 'Peso Bonus Gol'),
('M_PESO_ASSIST', 0.05, 'Peso Bonus Assist');

-- Tabella Commento
CREATE TABLE Commento (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_formazione INT NOT NULL, -- formazione a cui si riferisce
    id_utente INT NOT NULL,     --utente che scrive commento
    contenuto TEXT NOT NULL,
    data_creazione TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_formazione) REFERENCES Formazione(id) ON DELETE CASCADE,
    FOREIGN KEY (id_utente) REFERENCES Utente(id) ON DELETE CASCADE
);

-- voto al consiglio
CREATE TABLE Reazione (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_commento INT NOT NULL,   -- id del commento da recensire
    id_utente INT NOT NULL,     -- id utente che vota
    voto INT NOT NULL,          -- +1 o -1
    UNIQUE KEY unique_reazione (id_commento, id_utente),
    FOREIGN KEY (id_commento) REFERENCES Commento(id) ON DELETE CASCADE,
    FOREIGN KEY (id_utente) REFERENCES Utente(id) ON DELETE CASCADE
);
