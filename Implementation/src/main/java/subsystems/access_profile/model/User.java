package subsystems.access_profile.model;

/**
 * Rappresenta l'utente registrato nel sistema.
 */
public class User {

    private String nome;
    private String cognome;
    private String email;
    private String username;
    private String password; // Hash della password
    private Role role;

    public User() {}

    public User(String email, String username, String password, String nome, String cognome, Role role) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.nome = nome;
        this.cognome = cognome;
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    @Override
    public String toString() {
        return "User [email=" + email + ", username=" + username + ", role=" + role + "]";
    }
}
