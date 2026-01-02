package subsystems.access_profile.control;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import subsystems.access_profile.model.User;
import subsystems.access_profile.model.UserDAO;
import utils.PasswordHasher;

/**
 * Descrizione: Gestisce specificamente la modifica dei dati del profilo utente.
 */
@WebServlet("/UpdateProfileServlet")
public class UpdateProfileServlet extends HttpServlet {

    // Action: update Profile
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        User currentUser = (User) session.getAttribute("user");

        String nuovoNome = request.getParameter("nome");
        String nuovoCognome = request.getParameter("cognome");
        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");

        UserDAO userDAO = new UserDAO();

        if (nuovoNome != null && !nuovoNome.trim().isEmpty()) {
            currentUser.setNome(nuovoNome);
        }
        if (nuovoCognome != null && !nuovoCognome.trim().isEmpty()) {
            currentUser.setCognome(nuovoCognome);
        }

        // "Per modificare la password, l'utente deve fornire la vecchia password per conferma."
        boolean passwordChangeRequested = (newPassword != null && !newPassword.trim().isEmpty());

        if (passwordChangeRequested) {
            if (oldPassword == null || oldPassword.trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Per cambiare password devi inserire la vecchia password.");
                return;
            }

            // Verifica che la vecchia password corrisponda all'hash nel DB
            if (!PasswordHasher.verify(oldPassword, currentUser.getPassword())) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "La vecchia password non è corretta.");
                return;
            }

            // Se corretta, eseguiamo l'hash della nuova e aggiorniamo l'oggetto
            String newHashedPassword = PasswordHasher.hash(newPassword);
            currentUser.setPassword(newHashedPassword);
        }

        try {
            userDAO.doUpdateProfile(currentUser);
            session.setAttribute("user", currentUser);

            response.sendRedirect("ProfileServlet?status=updated");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante l'aggiornamento del profilo.");
        }
    }
}