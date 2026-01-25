package subsystems.community.control;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import subsystems.access_profile.model.User;
import subsystems.community.model.*;
import subsystems.team_management.model.Formation;
import subsystems.team_management.model.FormationDAO;
import utils.ReactionUtils;


@WebServlet("/PostServlet")
public class PostServlet extends HttpServlet {

    // Gestione pubblicazione nuovi post
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/view/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");

        // --- LOGICA DI RECUPERO DATI (IBRIDA: ATTRIBUTI O PARAMETRI) ---

        // 1. Recupero TESTO
        // Prima controlla se è stato passato come attributo (dal forward), altrimenti dal form parameter
        Object testoAttr = request.getAttribute("testo");
        String testo = (testoAttr != null) ? (String) testoAttr : request.getParameter("testo");

        // 2. Recupero ID FORMAZIONE
        // Prima controlla attributi (nuova formazione appena creata), altrimenti parameter
        Object formationIdAttr = request.getAttribute("formationId");
        String formationIdParam = request.getParameter("formationId");

        Integer formationId = null;

        // Caso A: Arrivo da FormationServlet (è un Integer nell'attributo)
        if (formationIdAttr != null) {
            if (formationIdAttr instanceof Integer) {
                formationId = (Integer) formationIdAttr;
            } else {
                // Caso raro: è una stringa nell'attributo
                try { formationId = Integer.parseInt(formationIdAttr.toString()); } catch (Exception e) {}
            }
        }
        // Caso B: Arrivo da form normale (è una Stringa nel parametro)
        else if (formationIdParam != null && !formationIdParam.trim().isEmpty()) {
            try {
                formationId = Integer.parseInt(formationIdParam);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        // --- VALIDAZIONE ---

        boolean hasText = (testo != null && !testo.trim().isEmpty());
        boolean hasFormation = (formationId != null);

        // Se non c'è né testo né formazione, è un errore
        if (!hasText && !hasFormation) {
            response.sendRedirect(request.getContextPath() + "/PostServlet?error=EmptyContent");            return;
        }

        if (testo == null) testo = "";

        // --- SALVATAGGIO ---

        Post post = new Post(user.getEmail(), testo, formationId);
        PostDAO postDAO = new PostDAO();

        try {
            postDAO.doSave(post);
            // Redirect pulito alla pagina community/feed
            response.sendRedirect(request.getContextPath() + "/PostServlet");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore pubblicazione post");
        }
    }

    // Gestione visualizzazione bacheca
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Recupero Utente in sessione (può essere null se visitatore)
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        // Istanza dei DAO necessari
        PostDAO postDAO = new PostDAO();
        CommentDAO commentDAO = new CommentDAO();
        ReactionDAO reactionDAO = new ReactionDAO();
        FormationDAO formationDAO = new FormationDAO(); // <--- Aggiunto questo

        try {
            // 1. Recupera tutti i post dal DB
            List<Post> posts = postDAO.doRetrieveAll();

            // 2. Arricchisce ogni post con i dettagli (Commenti, Reazioni, Formazioni)
            for (Post p : posts) {

                // A. Recupera i commenti
                p.setComments(commentDAO.doRetrieveByPostId(p.getId()));

                // B. Calcola il numero di reazioni (Like, etc.)
                Map<String, Integer> counts = ReactionUtils.calculateReactionCounts(p.getId());
                p.setReactionCounts(counts);

                // C. Se l'utente è loggato, controlla se ha già messo like
                if (user != null) {
                    String userReaction = reactionDAO.doRetrieveUserReaction(user.getEmail(), p.getId());
                    p.setCurrentUserReaction(userReaction);
                }

                // D. RECUPERO FORMAZIONE (Il pezzo mancante)
                // Se il post ha un ID formazione valido, carichiamo i dettagli completi (giocatori)
                if (p.getFormationId() != null && p.getFormationId() > 0) {
                    Formation fullFormation = formationDAO.doRetrieveById(p.getFormationId());
                    p.setFormation(fullFormation);
                }
            }

            // 3. Passa la lista completa alla JSP
            request.setAttribute("posts", posts);
            request.getRequestDispatcher("view/community.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace(); // Utile per debug
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore caricamento feed");
        }
    }
}