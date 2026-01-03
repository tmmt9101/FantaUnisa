package subsystems.community.model;

import connection.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostDAO {

    public void doSave(Post post) {
        String query = "INSERT INTO post (user_email, testo, data_ora, formation_id) VALUES (?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, post.getUserEmail());
            ps.setString(2, post.getTesto());
            ps.setTimestamp(3, post.getDataOra());

            if (post.getFormationId() != null) {
                ps.setInt(4, post.getFormationId());
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore durante il salvataggio del post", e);
        }
    }

    public List<Post> doRetrieveAll() {
        List<Post> posts = new ArrayList<>();
        String query = "SELECT * FROM post ORDER BY data_ora DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Post post = new Post();
                post.setId(rs.getInt("id"));
                post.setUserEmail(rs.getString("user_email"));
                post.setTesto(rs.getString("testo"));
                post.setDataOra(rs.getTimestamp("data_ora"));

                int formationId = rs.getInt("formation_id");
                if (!rs.wasNull()) {
                    post.setFormationId(formationId);
                }

                posts.add(post);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore recupero posts", e);
        }
        return posts;
    }

    public Post doRetrieveById(int id) {
        String query = "SELECT * FROM post WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Post post = new Post();
                    post.setId(rs.getInt("id"));
                    post.setUserEmail(rs.getString("user_email"));
                    post.setTesto(rs.getString("testo"));
                    post.setDataOra(rs.getTimestamp("data_ora"));
                    int fId = rs.getInt("formation_id");
                    if (!rs.wasNull()) post.setFormationId(fId);
                    return post;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void doDelete(int id) {
        String query = "DELETE FROM post WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Errore cancellazione post", e);
        }
    }

}
