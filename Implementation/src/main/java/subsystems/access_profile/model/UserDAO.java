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
        String query = "INSERT INTO user (nome, cognome, email, username, password, ruolo) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, user.getNome());
            ps.setString(2, user.getCognome());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getUsername());
            ps.setString(5, user.getPassword()); // Hash già calcolato
            ps.setString(6, user.getRole().name()); // Enum -> String (es. "FANTALLENATORE")

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore salvataggio utente", e);
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
     * Aggiorna i dati del profilo (Nome, Cognome, Password).
     * Username e Email sono invarianti (non modificabili) come da ODD[cite: 39].
     * Riferimento ODD: doUpdateProfile(user: User): void
     */
    public void doUpdateProfile(User user) {
        String query = "UPDATE user SET nome = ?, cognome = ?, password = ? WHERE email = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, user.getNome());
            ps.setString(2, user.getCognome());
            ps.setString(3, user.getPassword()); // Hash aggiornato
            ps.setString(4, user.getEmail());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore aggiornamento profilo", e);
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
}
