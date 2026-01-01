-- Script per importare i dati dal CSV con una tabella temporanea

-- creazione tabella temporanea di appoggio
CREATE TEMPORARY TABLE IF NOT EXISTS Staging_Import (
    Id INT,
    R VARCHAR(5),
    Nome VARCHAR(100),
    Squadra VARCHAR(50),
    Pv INT,
    Mv_Str VARCHAR(10), -- Importiamo come stringa per gestire la virgola (es. "6,5")
    Fm_Str VARCHAR(10),
    Gf INT,
    Gs INT,
    Rp INT,
    Rc INT,
    R_piu INT,
    R_meno INT,
    Ass INT,
    Amm INT,
    Esp INT,
    Au INT
);

-- caricamento dati dal CSV
LOAD DATA LOCAL INFILE 'percorso del file .csv'
INTO TABLE Staging_Import
FIELDS TERMINATED BY ';'
LINES TERMINATED BY '\r\n'
IGNORE 1 ROWS; -- Salta l'intestazione

-- popolamento tabella squadre
INSERT IGNORE INTO SquadraSerieA (nome)
SELECT DISTINCT Squadra FROM Staging_Import;

-- inserimento/update calciatori
INSERT INTO Calciatore (id, nome, id_squadra_serie_a, ruolo)
SELECT s.Id, s.Nome, sa.id, s.R
FROM Staging_Import s
JOIN SquadraSerieA sa ON s.Squadra = sa.nome
ON DUPLICATE KEY UPDATE
    id_squadra_serie_a = VALUES(id_squadra_serie_a),
    ruolo = VALUES(ruolo);

-- inserimento statistiche
INSERT INTO Statistica (
    id_calciatore, giornata, partite_voto, media_voto, fanta_media,
    gol_fatti, gol_subiti, rigori_parati, rigori_calciati,
    rigori_segnati, rigori_sbagliati, assist, ammonizioni, espulsioni, autogol
)
SELECT
    Id,
    17, -- da impostare manualmente
    Pv,
    CAST(REPLACE(Mv_Str, ',', '.') AS DECIMAL(4,2)), -- Converte "6,5" in 6.5
    CAST(REPLACE(Fm_Str, ',', '.') AS DECIMAL(4,2)),
    Gf, Gs, Rp, Rc, R_piu, R_meno, Ass, Amm, Esp, Au
FROM Staging_Import
ON DUPLICATE KEY UPDATE
    partite_voto = VALUES(partite_voto),
    media_voto = VALUES(media_voto),
    fanta_media = VALUES(fanta_media),
    gol_fatti = VALUES(gol_fatti),
    gol_subiti = VALUES(gol_subiti),
    rigori_parati = VALUES(rigori_parati),
    rigori_calciati = VALUES(rigori_calciati),
    rigori_segnati = VALUES(rigori_segnati),
    rigori_sbagliati = VALUES(rigori_sbagliati),
    assist = VALUES(assist),
    ammonizioni = VALUES(ammonizioni),
    espulsioni = VALUES(espulsioni),
    autogol = VALUES(autogol);

-- Pulizia
DROP TEMPORARY TABLE Staging_Import;
