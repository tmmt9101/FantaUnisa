package it.unisa.fantaunisa.persistence;

import it.unisa.fantaunisa.model.Utente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UtenteDAO {

    //salva un nuovo utente nel database
    public void doSave(Utente utente) {
        try (Connection con = DriverManagerConnectionPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO Utente (username, email, password_hash, ruolo, punteggio_reputazione) VALUES (?, ?, ?, ?, ?)"
            );
            ps.setString(1, utente.getUsername());
            ps.setString(2, utente.getEmail());
            ps.setString(3, utente.getPasswordHash());
            ps.setString(4, utente.getRuolo());
            ps.setInt(5, utente.getPunteggioReputazione());
            
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //cerca un utente per email e password (login)
    public Utente doRetrieveByEmailPassword(String email, String passwordHash) {
        try (Connection con = DriverManagerConnectionPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM Utente WHERE email = ? AND password_hash = ?"
            );
            ps.setString(1, email);
            ps.setString(2, passwordHash);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Utente utente = new Utente();
                utente.setId(rs.getInt("id"));
                utente.setUsername(rs.getString("username"));
                utente.setEmail(rs.getString("email"));
                utente.setPasswordHash(rs.getString("password_hash"));
                utente.setRuolo(rs.getString("ruolo"));
                utente.setPunteggioReputazione(rs.getInt("punteggio_reputazione"));
                return utente;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //aggiorna il punteggio reputazione di un utente
    public void updateReputazione(int idUtente, int nuovoPunteggio) {
        try (Connection con = DriverManagerConnectionPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                "UPDATE Utente SET punteggio_reputazione = ? WHERE id = ?"
            );
            ps.setInt(1, nuovoPunteggio);
            ps.setInt(2, idUtente);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
