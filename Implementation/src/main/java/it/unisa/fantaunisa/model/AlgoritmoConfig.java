package it.unisa.fantaunisa.model;

import java.io.Serializable;

public class AlgoritmoConfig implements Serializable {
    
    //pesi portieri
    private double pesoMvPortiere;
    private double pesoGsPortiere;
    private double pesoCostanzaPortiere;

    //pesi giocatori di movimento
    private double pesoFmGiocatore;       
    private double pesoCostanzaGiocatore; 
    private double pesoGol;               
    private double pesoAssist;            

    public double getPesoMvPortiere() { return pesoMvPortiere; }
    public void setPesoMvPortiere(double pesoMvPortiere) { this.pesoMvPortiere = pesoMvPortiere; }

    public double getPesoGsPortiere() { return pesoGsPortiere; }
    public void setPesoGsPortiere(double pesoGsPortiere) { this.pesoGsPortiere = pesoGsPortiere; }

    public double getPesoCostanzaPortiere() { return pesoCostanzaPortiere; }
    public void setPesoCostanzaPortiere(double pesoCostanzaPortiere) { this.pesoCostanzaPortiere = pesoCostanzaPortiere; }

    public double getPesoFmGiocatore() { return pesoFmGiocatore; }
    public void setPesoFmGiocatore(double pesoFmGiocatore) { this.pesoFmGiocatore = pesoFmGiocatore; }

    public double getPesoCostanzaGiocatore() { return pesoCostanzaGiocatore; }
    public void setPesoCostanzaGiocatore(double pesoCostanzaGiocatore) { this.pesoCostanzaGiocatore = pesoCostanzaGiocatore; }

    public double getPesoGol() { return pesoGol; }
    public void setPesoGol(double pesoGol) { this.pesoGol = pesoGol; }

    public double getPesoAssist() { return pesoAssist; }
    public void setPesoAssist(double pesoAssist) { this.pesoAssist = pesoAssist; }
}
