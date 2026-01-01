package it.unisa.fantaunisa.model;

public class Statistiche {
    private int id;
    private int idCalciatore;
    private int giornata;
    private int partiteVoto;
    private double mediaVoto;
    private double fantaMedia;
    private int golFatti;
    private int golSubiti;
    private int rigoriParati;
    private int rigoriCalciati;
    private int rigoriSegnati;
    private int rigoriSbagliati;
    private int assist;
    private int ammonizioni;
    private int espulsioni;
    private int autogol;

    public Statistiche() {
    }

    public Statistiche(int partiteVoto, double mediaVoto, double fantaMedia, int golFatti, int golSubiti, int rigoriParati, int rigoriSegnati, int rigoriSbagliati, int assist, int ammonizioni, int espulsioni, int autogol) {
        this.partiteVoto = partiteVoto;
        this.mediaVoto = mediaVoto;
        this.fantaMedia = fantaMedia;
        this.golFatti = golFatti;
        this.golSubiti = golSubiti;
        this.rigoriParati = rigoriParati;
        this.rigoriSegnati = rigoriSegnati;
        this.rigoriSbagliati = rigoriSbagliati;
        this.assist = assist;
        this.ammonizioni = ammonizioni;
        this.espulsioni = espulsioni;
        this.autogol = autogol;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdCalciatore() { return idCalciatore; }
    public void setIdCalciatore(int idCalciatore) { this.idCalciatore = idCalciatore; }

    public int getGiornata() { return giornata; }
    public void setGiornata(int giornata) { this.giornata = giornata; }

    public int getPartiteVoto() { return partiteVoto; }
    public void setPartiteVoto(int partiteVoto) { this.partiteVoto = partiteVoto; }

    public double getMediaVoto() { return mediaVoto; }
    public void setMediaVoto(double mediaVoto) { this.mediaVoto = mediaVoto; }

    public double getFantaMedia() { return fantaMedia; }
    public void setFantaMedia(double fantaMedia) { this.fantaMedia = fantaMedia; }

    public int getGolFatti() { return golFatti; }
    public void setGolFatti(int golFatti) { this.golFatti = golFatti; }

    public int getGolSubiti() { return golSubiti; }
    public void setGolSubiti(int golSubiti) { this.golSubiti = golSubiti; }

    public int getRigoriParati() { return rigoriParati; }
    public void setRigoriParati(int rigoriParati) { this.rigoriParati = rigoriParati; }

    public int getRigoriCalciati() { return rigoriCalciati; }
    public void setRigoriCalciati(int rigoriCalciati) { this.rigoriCalciati = rigoriCalciati; }

    public int getRigoriSegnati() { return rigoriSegnati; }
    public void setRigoriSegnati(int rigoriSegnati) { this.rigoriSegnati = rigoriSegnati; }

    public int getRigoriSbagliati() { return rigoriSbagliati; }
    public void setRigoriSbagliati(int rigoriSbagliati) { this.rigoriSbagliati = rigoriSbagliati; }

    public int getAssist() { return assist; }
    public void setAssist(int assist) { this.assist = assist; }

    public int getAmmonizioni() { return ammonizioni; }
    public void setAmmonizioni(int ammonizioni) { this.ammonizioni = ammonizioni; }

    public int getEspulsioni() { return espulsioni; }
    public void setEspulsioni(int espulsioni) { this.espulsioni = espulsioni; }

    public int getAutogol() { return autogol; }
    public void setAutogol(int autogol) { this.autogol = autogol; }
}
