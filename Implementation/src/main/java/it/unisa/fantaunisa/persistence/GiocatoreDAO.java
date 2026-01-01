package it.unisa.fantaunisa.persistence;

import it.unisa.fantaunisa.model.Giocatore;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GiocatoreDAO {

    //salva un giocatore nel database (usato nell'importazione)
    public void doSave(Giocatore giocatore) {
        try (Connection con = DriverManagerConnectionPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO Calciatore (id, nome, squadra, ruolo, quotazione) VALUES (?, ?, ?, ?, ?)"
            );
            ps.setInt(1, giocatore.getId());
            ps.setString(2, giocatore.getNome());
            ps.setString(3, giocatore.getSquadraSerieA());
            ps.setString(4, giocatore.getRuolo());
            ps.setInt(5, 1); //quotazione default 1 per ora
            
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //recupera tutti i giocatori (listone)
    public List<Giocatore> doRetrieveAll() {
        List<Giocatore> giocatori = new ArrayList<>();
        try (Connection con = DriverManagerConnectionPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Calciatore");
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Giocatore g = new Giocatore();
                g.setId(rs.getInt("id"));
                g.setNome(rs.getString("nome"));
                g.setSquadraSerieA(rs.getString("squadra"));
                g.setRuolo(rs.getString("ruolo"));
                giocatori.add(g);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return giocatori;
    }

    //recupera un giocatore per id
    public Giocatore doRetrieveById(int id) {
        try (Connection con = DriverManagerConnectionPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Calciatore WHERE id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                Giocatore g = new Giocatore();
                g.setId(rs.getInt("id"));
                g.setNome(rs.getString("nome"));
                g.setSquadraSerieA(rs.getString("squadra"));
                g.setRuolo(rs.getString("ruolo"));
                return g;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
