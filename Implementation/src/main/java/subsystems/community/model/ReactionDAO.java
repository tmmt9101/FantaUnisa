package subsystems.community.model;

import connection.DBConnection;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ReactionDAO {

    /**
     * Salva o Aggiorna una reazione.
     * Se l'utente ha già reagito a questo post, aggiorna il tipo di reazione.
     */
    public void doSaveOrUpdate(Reaction reaction) {
        String query = "INSERT INTO reaction (user_email, post_id, tipo) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE tipo = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, reaction.getUserEmail());
            ps.setInt(2, reaction.getPostId());
            ps.setString(3, reaction.getTipo());

            // Parametro per l'UPDATE (se esiste già)
            ps.setString(4, reaction.getTipo());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore salvataggio reaction", e);
        }
    }

    /**
     * Rimuove la reazione (es. quando l'utente clicca di nuovo su Like per toglierlo).
     */
    public void doDelete(String userEmail, int postId) {
        String query = "DELETE FROM reaction WHERE user_email = ? AND post_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, userEmail);
            ps.setInt(2, postId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore cancellazione reaction", e);
        }
    }

    /**
     * Recupera la reazione di uno specifico utente su un post (per sapere se ha già messo Like).
     * Restituisce null se non c'è reazione.
     */
    public String doRetrieveUserReaction(String userEmail, int postId) {
        String query = "SELECT tipo FROM reaction WHERE user_email = ? AND post_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, userEmail);
            ps.setInt(2, postId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("tipo");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Nessuna reazione trovata
    }
}