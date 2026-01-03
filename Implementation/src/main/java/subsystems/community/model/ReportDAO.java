package subsystems.community.model;

import connection.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    public void doSave(Report report) {
        String query = "INSERT INTO report (user_email, post_id, motivo, data_ora) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, report.getUserEmail());
            ps.setInt(2, report.getPostId());
            ps.setString(3, report.getMotivo());
            ps.setTimestamp(4, report.getDataOra());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore creazione report", e);
        }
    }

    public List<Report> doRetrieveAll() {
        List<Report> reports = new ArrayList<>();
        String query = "SELECT * FROM report ORDER BY data_ora DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Report r = new Report();
                r.setId(rs.getInt("id"));
                r.setUserEmail(rs.getString("user_email"));
                r.setPostId(rs.getInt("post_id"));
                r.setMotivo(rs.getString("motivo"));
                r.setDataOra(rs.getTimestamp("data_ora"));
                reports.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reports;
    }

    public void doDelete(int id) {
        String query = "DELETE FROM report WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
