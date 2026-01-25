package subsystems.access_profile.control;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import subsystems.access_profile.model.Role;
import subsystems.access_profile.model.User;
import subsystems.access_profile.model.UserDAO;
import subsystems.community.model.Post;
import subsystems.community.model.PostDAO;

@WebServlet("/DeleteUserServlet")
public class DeleteUserServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

        // FIX PATH: Redirect assoluto
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/view/login.jsp");
            return;
        }

        String emailToDelete = request.getParameter("email");
        String postIdStr = request.getParameter("postId"); // Nuovo parametro
        String from = request.getParameter("from");        // Per sapere dove tornare

        UserDAO userDAO = new UserDAO();

        // CASO A: Ban tramite Post ID (dalla pagina di Moderazione)
        if (emailToDelete == null && postIdStr != null) {
            PostDAO postDAO = new PostDAO();
            try {
                int postId = Integer.parseInt(postIdStr);
                Post p = postDAO.doRetrieveById(postId);
                if (p != null) {
                    emailToDelete = p.getUserEmail(); // Troviamo l'autore del post
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // CASO B: Cancellazione profilo personale (nessun parametro)
        if (emailToDelete == null || emailToDelete.isEmpty()) {
            emailToDelete = currentUser.getEmail();
        }

        // CONTROLLO PERMESSI
        boolean isSelfDelete = currentUser.getEmail().equals(emailToDelete);
        boolean isAdmin = (currentUser.getRole() == Role.GESTORE_UTENTI);

        if (isSelfDelete || isAdmin) {

            // Eseguiamo l'eliminazione (Il DB a cascata cancellerà post e report dell'utente)
            userDAO.doDelete(emailToDelete);

            if (isSelfDelete) {
                // Caso 1: Mi sono cancellato -> Logout e Login
                session.invalidate();
                response.sendRedirect(request.getContextPath() + "/view/login.jsp?msg=AccountDeleted");
            } else {
                // Caso 2: Admin ha bannato -> Torna alla pagina corretta
                if ("admin".equals(from)) {
                    // Torna alle segnalazioni
                    response.sendRedirect(request.getContextPath() + "/ReportServlet?msg=UserBanned");
                } else {
                    // Torna a una generica gestione utenti (se esiste) o alla home
                    response.sendRedirect(request.getContextPath() + "/view/index.jsp?msg=UserBanned");
                }
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/view/home_utente.jsp?error=Unauthorized");
        }
    }
}