package it.unisa.fantaunisa.model;

public class Giocatore {
    private int id;
    private String nome;
    private String ruolo; // "P", "D", "C", "A"
    private String squadraSerieA;
    private Statistiche statistiche; // Statistiche correnti per l'algoritmo

    public Giocatore() {
    }

    public Giocatore(int id, String nome, String ruolo, String squadraSerieA) {
        this.id = id;
        this.nome = nome;
        this.ruolo = ruolo;
        this.squadraSerieA = squadraSerieA;
    }

    //costruttore
    public Giocatore(int id, String nome, String ruolo, Statistiche statistiche) {
        this.id = id;
        this.nome = nome;
        this.ruolo = ruolo;
        this.statistiche = statistiche;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getRuolo() { return ruolo; }
    public void setRuolo(String ruolo) { this.ruolo = ruolo; }

    public String getSquadraSerieA() { return squadraSerieA; }
    public void setSquadraSerieA(String squadraSerieA) { this.squadraSerieA = squadraSerieA; }

    public Statistiche getStatistiche() { return statistiche; }
    public void setStatistiche(Statistiche statistiche) { this.statistiche = statistiche; }
}
