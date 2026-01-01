package it.unisa.fantaunisa.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import it.unisa.fantaunisa.model.Giocatore;
import it.unisa.fantaunisa.model.Statistiche;
import it.unisa.fantaunisa.model.AlgoritmoConfig;

public class CalcolaFormazione {

    public List<Giocatore> calcolaFormazione(List<Giocatore> rosa, String modulo, int giornataCorrente, AlgoritmoConfig config) {
        
        //creiamo 4 liste separate per ruolo
        List<Giocatore> portieri = new ArrayList<>();
        List<Giocatore> difensori = new ArrayList<>();
        List<Giocatore> centrocampisti = new ArrayList<>();
        List<Giocatore> attaccanti = new ArrayList<>();

        //dividiamo la rosa nelle 4 liste
        for (Giocatore g : rosa) {
            if (g.getRuolo().equalsIgnoreCase("P")) portieri.add(g);
            else if (g.getRuolo().equalsIgnoreCase("D")) difensori.add(g);
            else if (g.getRuolo().equalsIgnoreCase("C")) centrocampisti.add(g);
            else if (g.getRuolo().equalsIgnoreCase("A")) attaccanti.add(g);
        }

        //ordiniamo ogni lista in base al punteggio
        ordinaPerPunteggio(portieri, giornataCorrente, config);
        ordinaPerPunteggio(difensori, giornataCorrente, config);
        ordinaPerPunteggio(centrocampisti, giornataCorrente, config);
        ordinaPerPunteggio(attaccanti, giornataCorrente, config);

        //estraiamo gli slot a partire dal modulo (es. 3-4-3)
        String[] reparti = modulo.split("-");
        int numDif = Integer.parseInt(reparti[0]);
        int numCen = Integer.parseInt(reparti[1]);
        int numAtt = Integer.parseInt(reparti[2]);

        List<Giocatore> formazioneFinale = new ArrayList<>();

        //inseriamo i titolari, prima il portiere, poi il resto in base agli slot del modulo

        aggiungiGiocatori(formazioneFinale, portieri, 0, 1);
        aggiungiGiocatori(formazioneFinale, difensori, 0, numDif);
        aggiungiGiocatori(formazioneFinale, centrocampisti, 0, numCen);
        aggiungiGiocatori(formazioneFinale, attaccanti, 0, numAtt);

        //dal 12 slot iniziano i panchinari nello stesso ordine di ruolo
        aggiungiGiocatori(formazioneFinale, portieri, 1, portieri.size());
        aggiungiGiocatori(formazioneFinale, difensori, numDif, difensori.size());
        aggiungiGiocatori(formazioneFinale, centrocampisti, numCen, centrocampisti.size());
        aggiungiGiocatori(formazioneFinale, attaccanti, numAtt, attaccanti.size());
        return formazioneFinale;
    }

    //metodo per ordinare una lista
    private void ordinaPerPunteggio(List<Giocatore> lista, int giornata, AlgoritmoConfig config) {
        Collections.sort(lista, new Comparator<Giocatore>() {
            @Override
            public int compare(Giocatore g1, Giocatore g2) {
                double p1 = calcolaPunteggio(g1, giornata, config);
                double p2 = calcolaPunteggio(g2, giornata, config);
                if (p1 < p2) return 1;
                if (p1 > p2) return -1;
                return 0;
            }
        });
    }

    //metodo per aggiungere una sottolista alla formazione
    private void aggiungiGiocatori(List<Giocatore> destinazione, List<Giocatore> fonte, int inizio, int fine) {
        for (int i = inizio; i < fine && i < fonte.size(); i++) {
            destinazione.add(fonte.get(i));
        }
    }

    private double calcolaPunteggio(Giocatore g, int giornataCorrente, AlgoritmoConfig config) {
        Statistiche s = g.getStatistiche();
        if (s == null) return 0.0; 

        double score = 0.0;
        
        //percentuale titolarità
        double costanza = ((double) s.getPartiteVoto() / giornataCorrente) * 100.0;

        if (g.getRuolo().equalsIgnoreCase("P")) {
            //portieri
            double mvNormalizzata = (s.getMediaVoto() / 8.0) * 100.0;
            
            //gol subiti
            double malusGol = ((double) s.getGolSubiti() / s.getPartiteVoto()) * 40.0; 
            double gsScore = 100.0 - malusGol;

            score = (mvNormalizzata * config.getPesoMvPortiere()) + 
                    (gsScore * config.getPesoGsPortiere()) + 
                    (costanza * config.getPesoCostanzaPortiere());

        } else {
            //giocatori di movimento            
            //1 fantamendia
            double fmNormalizzata = (s.getFantaMedia() / 11.0) * 100.0;

            //2 gol fatti
            double fattoreGol = ((double) s.getGolFatti() / s.getPartiteVoto()) * 100.0;

            // 3 fattore assist
            double fattoreAssist = ((double) s.getAssist() / s.getPartiteVoto()) * 100.0;

            score = (fmNormalizzata * config.getPesoFmGiocatore()) + 
                    (costanza * config.getPesoCostanzaGiocatore()) +
                    (fattoreGol * config.getPesoGol()) +
                    (fattoreAssist * config.getPesoAssist());
        }

        return score;
    }
}
