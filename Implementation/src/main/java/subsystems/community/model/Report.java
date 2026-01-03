package subsystems.community.model;

import java.sql.Timestamp;

public class Report {
    private int id;
    private String userEmail;
    private int postId;
    private String motivo;
    private Timestamp dataOra;

    public Report() {}

    public Report(String userEmail, int postId, String motivo) {
        this.userEmail = userEmail;
        this.postId = postId;
        this.motivo = motivo;
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

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public Timestamp getDataOra() {
        return dataOra;
    }

    public void setDataOra(Timestamp dataOra) {
        this.dataOra = dataOra;
    }
}