package subsystems.community.control;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import subsystems.access_profile.model.User;
import subsystems.community.model.Comment;
import subsystems.community.model.CommentDAO;

@WebServlet("/CommentServlet")
public class CommentServlet extends HttpServlet {

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
        String postIdStr = request.getParameter("postId");
        String formationIdStr = request.getParameter("formationId"); // Recupero parametro formazione

        if (postIdStr == null) {
            response.sendRedirect("community.jsp?error=InvalidPostId");
            return;
        }

        try {
            int postId = Integer.parseInt(postIdStr);
            Integer formationId = null;

            // Parsing ID Formazione (se presente)
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
                response.sendRedirect("community.jsp?error=EmptyComment");
                return;
            }

            if (testo == null) testo = "";

            Comment comment = new Comment(postId, user.getEmail(), testo, formationId);
            CommentDAO commentDAO = new CommentDAO();
            commentDAO.doSave(comment);

            response.sendRedirect("PostServlet");

        } catch (NumberFormatException e) {
            e.printStackTrace();
            response.sendRedirect("community.jsp?error=InvalidInput");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore salvataggio commento");
        }
    }
}