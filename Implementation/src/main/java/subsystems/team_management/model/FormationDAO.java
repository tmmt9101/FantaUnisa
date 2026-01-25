package subsystems.team_management.model;

import connection.DBConnection;
import java.sql.*;
import java.util.Map;

public class FormationDAO {

    public int doSave(Formation formation) {
        // Query per cancellare la vecchia formazione (se esiste)
        String deleteOldQuery = "DELETE FROM formation WHERE user_email = ? AND giornata = ?";

        // Query per inserire la nuova
        String queryHeader = "INSERT INTO formation (user_email, giornata, modulo) VALUES (?, ?, ?)";
        String queryDetail = "INSERT INTO formation_player (formation_id, player_id, posizione, tipo) VALUES (?, ?, ?, ?)";

        int generatedId = -1;

        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false); // Inizio Transazione

            try {
                // -----------------------------------------------------------
                // 0. PULIZIA: Cancelliamo la vecchia formazione di questa giornata
                // -----------------------------------------------------------
                // Nota: Se hai il "ON DELETE CASCADE" nel DB, cancellare la header cancella anche i giocatori.
                // Se non ce l'hai, cancella prima i giocatori, ma assumiamo che il DB sia ben fatto.
                try (PreparedStatement psDelete = con.prepareStatement(deleteOldQuery)) {
                    psDelete.setString(1, formation.getUserEmail());
                    psDelete.setInt(2, formation.getGiornata());
                    psDelete.executeUpdate();
                }

                // -----------------------------------------------------------
                // 1. INSERIMENTO TESTATA
                // -----------------------------------------------------------
                try (PreparedStatement psHeader = con.prepareStatement(queryHeader, Statement.RETURN_GENERATED_KEYS)) {
                    psHeader.setString(1, formation.getUserEmail());
                    psHeader.setInt(2, formation.getGiornata());
                    psHeader.setString(3, formation.getModulo());

                    psHeader.executeUpdate();

                    try (ResultSet rs = psHeader.getGeneratedKeys()) {
                        if (rs.next()) {
                            generatedId = rs.getInt(1);
                            formation.setId(generatedId);
                        } else {
                            throw new SQLException("Errore: Nessun ID generato.");
                        }
                    }
                }

                // -----------------------------------------------------------
                // 2. INSERIMENTO DETTAGLI
                // -----------------------------------------------------------
                try (PreparedStatement psDetail = con.prepareStatement(queryDetail)) {

                    int ordineInCampo = 1;

                    // Assicurati che Formation usi LinkedHashMap
                    for (Map.Entry<Integer, String> entry : formation.getPlayersMap().entrySet()) {
                        int playerId = entry.getKey();
                        String rawValue = entry.getValue();

                        String[] parts = rawValue.split(":");
                        String status = (parts.length > 1) ? parts[1] : "panchina";

                        psDetail.setInt(1, generatedId);
                        psDetail.setInt(2, playerId);
                        psDetail.setInt(3, ordineInCampo++);
                        psDetail.setString(4, status);

                        psDetail.addBatch();
                    }

                    psDetail.executeBatch();
                }

                con.commit(); // Conferma tutto (Delete + Insert)

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore SQL salvataggio formazione", e);
        }

        return generatedId;
    }

    public Formation doRetrieveById(int formationId) {
        Formation formation = null;

        // Query 1: Dati della formazione
        String headerSql = "SELECT * FROM formation WHERE id = ?";

        // Query 2: Giocatori (JOIN con player per avere il Ruolo P/D/C/A)
        // Ordiniamo per posizione (1,2,3...) per mantenere l'ordine visivo
        String playersSql = "SELECT p.id, p.nome, p.ruolo, fp.posizione, fp.tipo " +
                "FROM formation_player fp " +
                "JOIN player p ON fp.player_id = p.id " +
                "WHERE fp.formation_id = ? AND fp.tipo = 'titolare' " +
                "ORDER BY fp.posizione ASC";

        // 1. Primo Try: Connessione
        try (Connection con = DBConnection.getConnection()) {

            // 2. Secondo Try: Recupero Header Formazione
            try (PreparedStatement psHeader = con.prepareStatement(headerSql)) {
                psHeader.setInt(1, formationId);

                try (ResultSet rs = psHeader.executeQuery()) {
                    if (rs.next()) {
                        formation = new Formation();
                        formation.setId(formationId);
                        formation.setUserEmail(rs.getString("user_email"));
                        formation.setGiornata(rs.getInt("giornata"));
                        formation.setModulo(rs.getString("modulo"));
                    }
                }
            }

            // Se la formazione esiste, recuperiamo i giocatori
            if (formation != null) {
                // 3. Terzo Try: Recupero Giocatori
                try (PreparedStatement psPlayers = con.prepareStatement(playersSql)) {
                    psPlayers.setInt(1, formationId);

                    try (ResultSet rs = psPlayers.executeQuery()) {
                        while (rs.next()) {
                            Player p = new Player();
                            p.setId(rs.getInt("id"));
                            p.setNome(rs.getString("nome"));

                            // FONDAMENTALE: Prendiamo il ruolo dalla tabella PLAYER ('P', 'D', 'C', 'A')
                            // La JSP usa questo valore per decidere dove disegnare la maglietta
                            p.setRuolo(rs.getString("ruolo"));

                            // Aggiungiamo alla lista 'players' (quella usata dalla JSP)
                            formation.addFullPlayer(p);
                        }
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore recupero formazione", e);
        }

        return formation;
    }
}