package subsystems.team_management.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import subsystems.access_profile.model.User;
import subsystems.team_management.model.*;
import subsystems.module_selection.model.Module;
@WebServlet("/FormationServlet")
public class FormationServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) { response.sendRedirect("view/login.jsp"); return; }

        // 1. Recupera la Rosa dell'utente (per fargli scegliere i giocatori)
        SquadDAO squadDAO = new SquadDAO();
        Squad mySquad = squadDAO.doRetrieveSquadObject(user.getEmail()); // Usiamo il metodo "Wrapper" creato prima

        // 2. Recupera i moduli tattici disponibili
        List<Module> validModules = Module.getValidModules();

        // 3. Imposta attributi
        request.setAttribute("mySquad", mySquad);
        request.setAttribute("modules", validModules);

        // Passiamo la giornata corrente (hardcoded o da DB Settings)
        request.setAttribute("currentGiornata", 18); // Esempio: prossima giornata

        request.getRequestDispatcher("view/formazione.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // 0. Controllo Sessione
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) {
            response.sendRedirect("view/login.jsp");
            return;
        }

        try {
            // 1. Parametri base
            // Nota: gestisci eventuali NumberFormatException se necessario
            int giornata = Integer.parseInt(request.getParameter("giornata"));
            String moduloId = request.getParameter("modulo");

            // 2. Recupera l'array unico di stringhe dal JSP
            // Formato atteso per ogni stringa: "ID:RUOLO:STATUS" (es. "10:C:titolare")
            String[] giocatoriRaw = request.getParameterValues("giocatori");

            // VALIDAZIONE PRELIMINARE
            if (giocatoriRaw == null || giocatoriRaw.length == 0) {
                throw new IllegalArgumentException("Nessun giocatore selezionato.");
            }

            // 3. Creazione oggetto Formation
            Formation formation = new Formation(user.getEmail(), giornata, moduloId);

            // Liste di supporto per la validazione successiva
            List<Integer> titolariIds = new ArrayList<>();
            PlayerDAO playerDAO = new PlayerDAO();

            // 4. Parsing e Popolamento
            for (String rawData : giocatoriRaw) {
                // rawData es: "10:C:titolare"
                String[] parts = rawData.split(":");

                if (parts.length == 3) {
                    int pId = Integer.parseInt(parts[0]);
                    String pRuolo = parts[1];  // 'P', 'D', 'C', 'A' (per colonna 'posizione' DB)
                    String pStatus = parts[2]; // "titolare" o "panchina" (per colonna 'tipo' DB)

                    // TRUCCO PER IL DAO:
                    // Concateniamo Ruolo e Status per inserirli nel valore (String) della Mappa
                    // La stringa salvata nella mappa sarà: "C:titolare"
                    String mapValue = pRuolo + ":" + pStatus;

                    // Aggiungiamo alla formazione
                    formation.addPlayer(pId, mapValue);

                    // Se è titolare, lo aggiungiamo alla lista per validare la tattica dopo
                    if ("titolare".equals(pStatus)) {
                        titolariIds.add(pId);
                    }
                }
            }

            // 5. Validazione Regole
            if (titolariIds.size() != 11) {
                throw new IllegalArgumentException("Devi schierare esattamente 11 titolari. Ne risultano: " + titolariIds.size());
            }

            // Verifica esistenza modulo
            // Assumiamo che Module abbia un metodo statico o un service
            // (Nota: nel tuo codice originale usavi Module.findById, lo mantengo)
            // Module module = Module.findById(moduloId);
            // if (module == null) throw new IllegalArgumentException("Modulo non valido.");

            // Validazione Tattica (Ruoli coerenti col modulo)
            // Recuperiamo gli oggetti Player reali dal DB per essere sicuri dei ruoli
            List<Player> selectedStarters = new ArrayList<>();
            for (Integer id : titolariIds) {
                selectedStarters.add(playerDAO.doRetrieveById(id));
            }

            // validateTactics(selectedStarters, module); // Decommenta se hai il metodo e l'oggetto Module

            // 6. Salvataggio su DB
            FormationDAO formationDAO = new FormationDAO();
            int savedId = formationDAO.doSave(formation);

            String postText = request.getParameter("testo");

            if (postText != null && !postText.trim().isEmpty()) {
                // CASO "SCHIERA E PUBBLICA"
                // Qui puoi chiamare direttamente il Service/DAO del Post
                // Oppure fare un forward alla PostServlet passandogli gli attributi

                request.setAttribute("formationId", savedId);
                request.setAttribute("testo", postText);

                // Forward alla PostServlet che si occuperà di salvare il post e reindirizzare
                RequestDispatcher rd = request.getRequestDispatcher("/PostServlet");
                rd.forward(request, response);
                return; // Importante: interrompi qui l'esecuzione di FormationServlet
            }

            // 7. Successo
            request.setAttribute("formationId", savedId);
            // request.setAttribute("message", "Formazione salvata con successo!");
            request.getRequestDispatcher("/FormationServlet").forward(request, response);

        } catch (IllegalArgumentException e) {
            // Errori di logica (es. 10 titolari invece di 11)
            response.sendRedirect("calcola-formazione?error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
        } catch (Exception e) {
            // Errori di sistema
            e.printStackTrace();
            response.sendRedirect("calcola-formazione?error=Errore+di+Sistema");
        }
    }

    /**
     * Controlla se i giocatori scelti rispettano il conteggio del modulo.
     * Es. 3-4-3 -> Devono esserci 1 P, 3 D, 4 C, 3 A.
     */
    private void validateTactics(List<Player> players, Module module) {
        int p = 0, d = 0, c = 0, a = 0;

        for (Player pl : players) {
            switch (pl.getRuolo().toUpperCase()) {
                case "P": p++; break;
                case "D": d++; break;
                case "C": c++; break;
                case "A": a++; break;
            }
        }

        if (p != 1) throw new IllegalArgumentException("Devi schierare esattamente 1 portiere.");
        if (d != module.getDifensori()) throw new IllegalArgumentException("Il modulo richiede " + module.getDifensori() + " difensori.");
        if (c != module.getCentrocampisti()) throw new IllegalArgumentException("Il modulo richiede " + module.getCentrocampisti() + " centrocampisti.");
        if (a != module.getAttaccanti()) throw new IllegalArgumentException("Il modulo richiede " + module.getAttaccanti() + " attaccanti.");
    }
}