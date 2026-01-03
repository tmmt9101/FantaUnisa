package subsystems.community.model;

import connection.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentDAO {

    public void doSave(Comment comment) {
        // Query aggiornata con formation_id
        String query = "INSERT INTO comment (post_id, user_email, testo, data_ora, formation_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, comment.getPostId());
            ps.setString(2, comment.getUserEmail());
            ps.setString(3, comment.getTesto());
            ps.setTimestamp(4, comment.getDataOra());

            // Gestione del valore NULL per la formazione
            if (comment.getFormationId() != null) {
                ps.setInt(5, comment.getFormationId());
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore salvataggio commento", e);
        }
    }

    public List<Comment> doRetrieveByPostId(int postId) {
        List<Comment> comments = new ArrayList<>();
        String query = "SELECT * FROM comment WHERE post_id = ? ORDER BY data_ora ASC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, postId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Comment c = new Comment();
                    c.setId(rs.getInt("id"));
                    c.setPostId(rs.getInt("post_id"));
                    c.setUserEmail(rs.getString("user_email"));
                    c.setTesto(rs.getString("testo"));
                    c.setDataOra(rs.getTimestamp("data_ora"));

                    int fId = rs.getInt("formation_id");
                    if (!rs.wasNull()) {
                        c.setFormationId(fId);
                    }

                    comments.add(c);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore recupero commenti", e);
        }
        return comments;
    }

    public Comment doRetrieveById(int id) {
        String query = "SELECT * FROM comment WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Comment c = new Comment();
                    c.setId(rs.getInt("id"));
                    c.setPostId(rs.getInt("post_id"));
                    c.setUserEmail(rs.getString("user_email"));
                    c.setTesto(rs.getString("testo"));
                    c.setDataOra(rs.getTimestamp("data_ora"));
                    int fId = rs.getInt("formation_id");
                    if (!rs.wasNull()) {
                        c.setFormationId(fId);
                    }
                    return c;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void doDelete(int id) {
        String query = "DELETE FROM comment WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore cancellazione commento", e);
        }
    }

}