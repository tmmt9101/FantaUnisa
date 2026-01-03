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
import utils.ReactionUtils;


@WebServlet("/PostServlet")
public class PostServlet extends HttpServlet {

    // Gestione pubblicazione nuovi post
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");

        String testo = request.getParameter("testo");
        String formationIdStr = request.getParameter("formationId");

        Integer formationId = null;
        if (formationIdStr != null && !formationIdStr.trim().isEmpty()) {
            try {
                formationId = Integer.parseInt(formationIdStr);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Errore acquisizione formazione");
            }
        }

        boolean hasText = (testo != null && !testo.trim().isEmpty());
        boolean hasFormation = (formationId != null);

        if (!hasText && !hasFormation) {
            response.sendRedirect("community.jsp?error=EmptyContent");
            return;
        }

        if (testo == null) testo = "";

        Post post = new Post(user.getEmail(), testo, formationId);
        PostDAO postDAO = new PostDAO();

        try {
            postDAO.doSave(post);
            response.sendRedirect("PostServlet"); // Redirect al doGet per ricaricare la lista
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore pubblicazione post");
        }
    }

    // Gestione visualizzazione bacheca
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User user = (User) request.getSession().getAttribute("user");
        ReactionDAO reactionDAO = new ReactionDAO();
        PostDAO postDAO = new PostDAO();
        CommentDAO commentDAO = new CommentDAO();

        // 1. Recupera tutti i post
        List<Post> posts = postDAO.doRetrieveAll();

        // 2. Per ogni post, recupera i suoi commenti
        for (Post p : posts) {
            p.setComments(commentDAO.doRetrieveByPostId(p.getId()));

            Map<String, Integer> counts = ReactionUtils.calculateReactionCounts(p.getId());
            p.setReactionCounts(counts);

            if (user != null) {
                String userReaction = reactionDAO.doRetrieveUserReaction(user.getEmail(), p.getId());
                p.setCurrentUserReaction(userReaction);
            }
        }
        request.setAttribute("posts", posts);
        request.getRequestDispatcher("community.jsp").forward(request, response);
    }
}