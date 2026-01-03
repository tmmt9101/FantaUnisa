package utils;

import connection.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ReactionUtils {

    /**
     * Calcola il totale delle reazioni per tipo (LIKE, DISLIKE) dato un post.
     */
    public static Map<String, Integer> calculateReactionCounts(int postId) {
        Map<String, Integer> counts = new HashMap<>();

        counts.put("LIKE", 0);
        counts.put("DISLIKE", 0);

        String query = "SELECT tipo, COUNT(*) as totale FROM reaction WHERE post_id = ? GROUP BY tipo";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, postId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    counts.put(rs.getString("tipo"), rs.getInt("totale"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }
}
