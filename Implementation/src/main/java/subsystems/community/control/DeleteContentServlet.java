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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String type = request.getParameter("type"); // "post" o "comment"
        String idStr = request.getParameter("id");

        if (idStr == null || type == null) {
            response.sendRedirect("community.jsp?error=InvalidParams");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);

            if ("post".equals(type)) {
                handlePostDeletion(id, user, response);
            } else if ("comment".equals(type)) {
                handleCommentDeletion(id, user, response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("community.jsp?error=DeleteFailed");
        }
    }

    private void handlePostDeletion(int postId, User user, HttpServletResponse response) throws IOException {
        PostDAO postDAO = new PostDAO();
        Post post = postDAO.doRetrieveById(postId);

        if (post == null) {
            response.sendRedirect("community.jsp?error=NotFound");
            return;
        }

        // 2. CONTROLLO AUTORIZZAZIONE: Solo l'autore O l'Admin possono cancellare
        boolean isOwner = post.getUserEmail().equals(user.getEmail());
        boolean isAdmin = (user.getRole() == Role.GESTORE_UTENTI);

        if (isOwner || isAdmin) {
            postDAO.doDelete(postId);
            response.sendRedirect("PostServlet?msg=Deleted");
        } else {
            response.sendRedirect("community.jsp?error=Unauthorized");
        }
    }

    private void handleCommentDeletion(int commentId, User user, HttpServletResponse response) throws IOException {
        CommentDAO commentDAO = new CommentDAO();
        Comment comment = commentDAO.doRetrieveById(commentId);

        if (comment == null) {
            response.sendRedirect("community.jsp");
            return;
        }

        // Controllo Autorizzazione per commento
        boolean isOwner = comment.getUserEmail().equals(user.getEmail());
        boolean isAdmin = (user.getRole() == Role.GESTORE_UTENTI);

        if (isOwner || isAdmin) {
            commentDAO.doDelete(commentId);
            response.sendRedirect("PostServlet?msg=CommentDeleted#" + comment.getPostId());
        } else {
            response.sendRedirect("community.jsp?error=Unauthorized");
        }
    }
}