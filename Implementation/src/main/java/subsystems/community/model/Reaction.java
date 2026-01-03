package subsystems.community.model;

public class Reaction {
    private String userEmail;
    private int postId;
    private String tipo; // Es. "LIKE", "DISLIKE", "LOVE"

    public Reaction() {
    }

    public Reaction(String userEmail, int postId, String tipo) {
        this.userEmail = userEmail;
        this.postId = postId;
        this.tipo = tipo;
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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
