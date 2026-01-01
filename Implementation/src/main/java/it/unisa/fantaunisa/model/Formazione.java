package it.unisa.fantaunisa.model;

import java.util.ArrayList;
import java.util.List;

public class Formazione {
    private int id;
    private int idSquadra;
    private int giornata;
    private String modulo; // es. "4-4-2"
    private double totalePunti;
    
    // lsta ordinata: i primi 11 sono i titolari, i successivi sono la panchina
    private List<Giocatore> calciatori; 

    public Formazione() {
        this.calciatori = new ArrayList<>();
    }

    public Formazione(int idSquadra, int giornata, String modulo, List<Giocatore> calciatori) {
        this.idSquadra = idSquadra;
        this.giornata = giornata;
        this.modulo = modulo;
        this.calciatori = calciatori;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdSquadra() { return idSquadra; }
    public void setIdSquadra(int idSquadra) { this.idSquadra = idSquadra; }

    public int getGiornata() { return giornata; }
    public void setGiornata(int giornata) { this.giornata = giornata; }

    public String getModulo() { return modulo; }
    public void setModulo(String modulo) { this.modulo = modulo; }

    public double getTotalePunti() { return totalePunti; }
    public void setTotalePunti(double totalePunti) { this.totalePunti = totalePunti; }

    public List<Giocatore> getCalciatori() { return calciatori; }
    public void setCalciatori(List<Giocatore> calciatori) { this.calciatori = calciatori; }


    //metodi per ottenere titolari e panchinari da una formazione, serviranno???

    public List<Giocatore> getTitolari() {
        if (calciatori == null || calciatori.size() < 11) {
            return new ArrayList<>();
        }
        return calciatori.subList(0, 11);
    }


    public List<Giocatore> getPanchina() {
        if (calciatori == null || calciatori.size() <= 11) {
            return new ArrayList<>();
        }
        return calciatori.subList(11, calciatori.size());
    }
}
