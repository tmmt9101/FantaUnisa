package subsystems.community.model;

import java.sql.Timestamp;

public class Comment {
    private int id;
    private int postId;
    private String userEmail;  // Chi sta rispondendo
    private String testo;
    private Timestamp dataOra;
    private Integer formationId;

    public Comment() {
    }

    public Comment(int postId, String userEmail, String testo, Integer formationId) {
        this.postId = postId;
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

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
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
}