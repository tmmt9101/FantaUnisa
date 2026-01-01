package it.unisa.fantaunisa.model;

public class Utente {
    private int id;
    private String username;
    private String email;
    private String passwordHash;
    private String ruolo; // "Fantallenatore", "Gestore"
    private int punteggioReputazione; //punteggio basato sui voti ricevuti

    public Utente() {
    }

    public Utente(int id, String username, String email, String ruolo) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.ruolo = ruolo;
        this.punteggioReputazione = 0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRuolo() { return ruolo; }
    public void setRuolo(String ruolo) { this.ruolo = ruolo; }

    public int getPunteggioReputazione() { return punteggioReputazione; }
    public void setPunteggioReputazione(int punteggioReputazione) { this.punteggioReputazione = punteggioReputazione; }
}
