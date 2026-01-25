package subsystems.community.control;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import subsystems.access_profile.model.Role;
import subsystems.access_profile.model.User;
import subsystems.community.model.Comment;
import subsystems.community.model.CommentDAO;
import subsystems.community.model.Post;
import subsystems.community.model.PostDAO;

@WebServlet("/DeleteContentServlet")
public class DeleteContentServlet extends HttpServlet {

    // AGGIUNTA FONDAMENTALE: Gestisce le chiamate via Link (GET)
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Reindirizza la logica al doPost, così funziona sia con Link che con Form
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        // FIX PATH: Redirect assoluto
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/view/login.jsp");
            return;
        }

        String type = request.getParameter("type"); // "post" o "comment"
        String idStr = request.getParameter("id");
        String from = request.getParameter("from"); // "admin" o null

        // FIX PATH: Redirect assoluto con slash
        if (idStr == null || type == null) {
            response.sendRedirect(request.getContextPath() + "/view/community.jsp?error=InvalidParams");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);

            if ("post".equals(type)) {
                handlePostDeletion(id, user, response, request, from);
            } else if ("comment".equals(type)) {
                handleCommentDeletion(id, user, response, request);
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/view/community.jsp?error=DeleteFailed");
        }
    }

    private void handlePostDeletion(int postId, User user, HttpServletResponse response, HttpServletRequest request, String from) throws IOException {
        PostDAO postDAO = new PostDAO();
        Post post = postDAO.doRetrieveById(postId);

        if (post == null) {
            // Se il post non esiste, torniamo indietro
            if ("admin".equals(from)) {
                response.sendRedirect(request.getContextPath() + "/ReportServlet");
            } else {
                response.sendRedirect(request.getContextPath() + "/PostServlet?error=NotFound");
            }
            return;
        }

        // CONTROLLO AUTORIZZAZIONE
        boolean isOwner = post.getUserEmail().equals(user.getEmail());
        boolean isAdmin = (user.getRole() == Role.GESTORE_UTENTI);

        if (isOwner || isAdmin) {
            postDAO.doDelete(postId);

            // LOGICA INTELLIGENTE:
            // Se sono un admin, torno alle segnalazioni. Se sono utente, torno al feed.
            if ("admin".equals(from)) {
                // Torna alla lista report (ReportServlet doGet)
                response.sendRedirect(request.getContextPath() + "/ReportServlet?msg=PostDeleted");
            } else {
                // Torna al feed community
                response.sendRedirect(request.getContextPath() + "/PostServlet?msg=Deleted");
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/PostServlet?error=Unauthorized");
        }
    }

    private void handleCommentDeletion(int commentId, User user, HttpServletResponse response, HttpServletRequest request) throws IOException {
        CommentDAO commentDAO = new CommentDAO();
        Comment comment = commentDAO.doRetrieveById(commentId);

        if (comment == null) {
            response.sendRedirect(request.getContextPath() + "/PostServlet");
            return;
        }

        boolean isOwner = comment.getUserEmail().equals(user.getEmail());
        boolean isAdmin = (user.getRole() == Role.GESTORE_UTENTI);

        if (isOwner || isAdmin) {
            commentDAO.doDelete(commentId);
            // Torna al post specifico usando l'ancora #post-ID
            response.sendRedirect(request.getContextPath() + "/PostServlet?msg=CommentDeleted#post-" + comment.getPostId());
        } else {
            response.sendRedirect(request.getContextPath() + "/PostServlet?error=Unauthorized");
        }
    }
}