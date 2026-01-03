package subsystems.community.model;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public class Post {
    private int id;
    private String userEmail; // Autore del post
    private String testo;
    private Timestamp dataOra;
    private Integer formationId; // Integer (oggetto) perché può essere NULL
    private List<Comment> comments;
    private Map<String, Integer> reactionCounts;
    private String currentUserReaction;

    public Post() {
    }

    public Post(String userEmail, String testo, Integer formationId) {
        this.userEmail = userEmail;
        this.testo = testo;
        this.formationId = formationId;
        this.dataOra = new Timestamp(System.currentTimeMillis());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getTesto() {
        return testo;
    }

    public void setTesto(String testo) {
        this.testo = testo;
    }

    public Timestamp getDataOra() {
        return dataOra;
    }

    public void setDataOra(Timestamp dataOra) {
        this.dataOra = dataOra;
    }

    public Integer getFormationId() {
        return formationId;
    }

    public void setFormationId(Integer formationId) {
        this.formationId = formationId;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public Map<String, Integer> getReactionCounts() {
        return reactionCounts;
    }

    public void setReactionCounts(Map<String, Integer> reactionCounts) {
        this.reactionCounts = reactionCounts;
    }

    public String getCurrentUserReaction() {
        return currentUserReaction;
    }

    public void setCurrentUserReaction(String currentUserReaction) {
        this.currentUserReaction = currentUserReaction;
    }

    public int getLikeCount() {
        if (reactionCounts == null) return 0;
        return reactionCounts.getOrDefault("LIKE", 0);
    }

    public int getDislikeCount() {
        if (reactionCounts == null) return 0;
        return reactionCounts.getOrDefault("DISLIKE", 0);
    }
}