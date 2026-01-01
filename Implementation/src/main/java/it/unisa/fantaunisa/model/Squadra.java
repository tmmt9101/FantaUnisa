package it.unisa.fantaunisa.model;

import java.util.List;

public class Squadra {
    private int id;
    private int idUtente;
    private String nomeSquadra;
    private List<Giocatore> rosa;

    public Squadra() {
    }

    public Squadra(int id, int idUtente, String nomeSquadra) {
        this.id = id;
        this.idUtente = idUtente;
        this.nomeSquadra = nomeSquadra;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdUtente() { return idUtente; }
    public void setIdUtente(int idUtente) { this.idUtente = idUtente; }

    public String getNomeSquadra() { return nomeSquadra; }
    public void setNomeSquadra(String nomeSquadra) { this.nomeSquadra = nomeSquadra; }

    public List<Giocatore> getRosa() { return rosa; }
    public void setRosa(List<Giocatore> rosa) { this.rosa = rosa; }
}