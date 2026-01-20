package subsystems.access_profile.model;

import connection.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Descrizione: Gestisce la persistenza dei dati anagrafici e delle credenziali utente.
 */
public class UserDAO {

    /**
     * Salva un nuovo utente nel database.
     */
    public void doSave(User user) {
        String query = "INSERT INTO user (nome, cognome, email, username, password, ruolo, is_active, verification_token) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, user.getNome());
            ps.setString(2, user.getCognome());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getUsername());
            ps.setString(5, user.getPassword()); // Hash già calcolato
            ps.setString(6, user.getRole().name());
            ps.setBoolean(7, false);
            ps.setString(8, user.getVerificationToken());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore salvataggio utente", e);
        }
    }

    public boolean doActivate(String token) {
        String query = "UPDATE user SET is_active = TRUE, verification_token = NULL WHERE verification_token = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, token);
            int result = ps.executeUpdate();
            return result > 0; // Restituisce true se ha trovato e aggiornato l'utente

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Recupera un utente tramite email.
     */
    public User doRetrieveByEmail(String email) {
        String query = "SELECT * FROM user WHERE email = ?";
        User user = null;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user = new User();
                    user.setNome(rs.getString("nome"));
                    user.setCognome(rs.getString("cognome"));
                    user.setEmail(rs.getString("email"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));

                    // Conversione da Stringa DB a Enum Java
                    String ruoloStr = rs.getString("ruolo");
                    try {
                        user.setRole(Role.valueOf(ruoloStr));
                    } catch (IllegalArgumentException | NullPointerException e) {
                        // Fallback in caso di ruolo non valido nel DB
                        user.setRole(Role.FANTALLENATORE);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore recupero utente", e);
        }
        return user;
    }

    /**
     * Recupera utente per Login.
     */
    public User doRetrieveByEmailAndPassword(String email, String password) {
        String query = "SELECT * FROM user WHERE email = ? AND password = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean setResetToken(String email, String token) {
        // Scadenza 45 minuti
        String query = "UPDATE user SET reset_token = ?, reset_expiry = DATE_ADD(NOW(), INTERVAL 45 MINUTE) WHERE email = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, token);
            ps.setString(2, email);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Errore token reset", e);
        }
    }

    public User findByResetToken(String token) {
        String query = "SELECT * FROM user WHERE reset_token = ? AND reset_expiry > NOW()";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRowToUser(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public void updatePassword(String email, String newPassword) {
        String query = "UPDATE user SET password = ?, resetToken = NULL, resetExpiry = NULL WHERE email = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, newPassword);
            ps.setString(2, email);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore cambio password", e);
        }
    }

    public void doUpdateInfo(User user) {
        String query = "UPDATE user SET nome = ?, cognome = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, user.getNome());
            ps.setString(2, user.getCognome());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Errore aggiornamento info profilo", e);
        }
    }

    /**
     * Elimina un utente dal database.
     */
    public void doDelete(String email) {
        String query = "DELETE FROM user WHERE email = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, email);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore eliminazione utente", e);
        }
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setNome(rs.getString("nome"));
        user.setCognome(rs.getString("cognome"));
        user.setRole(Role.valueOf(rs.getString("ruolo")));
        user.setIs_active(rs.getBoolean("is_Active"));
        return user;
    }
}
