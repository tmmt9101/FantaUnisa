package it.unisa.fantaunisa.persistence;

import it.unisa.fantaunisa.model.AlgoritmoConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AlgoritmoConfigDAO {

    public AlgoritmoConfig doRetrieveAll() {
        AlgoritmoConfig config = new AlgoritmoConfig();
        
        try (Connection con = DriverManagerConnectionPool.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT chiave, valore FROM ConfigurazioneAlgoritmo");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String chiave = rs.getString("chiave");
                double valore = rs.getDouble("valore");

                //mappa le chiavi del DB ai campi dell'oggetto
                switch (chiave) {
                    case "P_PESO_MV": config.setPesoMvPortiere(valore); break;
                    case "P_PESO_GS": config.setPesoGsPortiere(valore); break;
                    case "P_PESO_COSTANZA": config.setPesoCostanzaPortiere(valore); break;
                    case "M_PESO_FM": config.setPesoFmGiocatore(valore); break;
                    case "M_PESO_COSTANZA": config.setPesoCostanzaGiocatore(valore); break;
                    case "M_PESO_GOL": config.setPesoGol(valore); break;
                    case "M_PESO_ASSIST": config.setPesoAssist(valore); break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return config;
    }

    public void doUpdate(AlgoritmoConfig config) {
        try (Connection con = DriverManagerConnectionPool.getConnection()) {
            //aggiorna ogni singolo peso
            updatePeso(con, "P_PESO_MV", config.getPesoMvPortiere());
            updatePeso(con, "P_PESO_GS", config.getPesoGsPortiere());
            updatePeso(con, "P_PESO_COSTANZA", config.getPesoCostanzaPortiere());
            updatePeso(con, "M_PESO_FM", config.getPesoFmGiocatore());
            updatePeso(con, "M_PESO_COSTANZA", config.getPesoCostanzaGiocatore());
            updatePeso(con, "M_PESO_GOL", config.getPesoGol());
            updatePeso(con, "M_PESO_ASSIST", config.getPesoAssist());
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updatePeso(Connection con, String chiave, double valore) throws SQLException {
        PreparedStatement ps = con.prepareStatement("UPDATE ConfigurazioneAlgoritmo SET valore = ? WHERE chiave = ?");
        ps.setDouble(1, valore);
        ps.setString(2, chiave);
        ps.executeUpdate();
    }
}
