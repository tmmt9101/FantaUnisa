package subsystems.access_profile.control;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import subsystems.access_profile.model.User;
import subsystems.access_profile.model.Role;
import subsystems.access_profile.model.UserDAO;
import utils.PasswordHasher;

/**
 * Descrizione: Gestisce la registrazione di nuovi utenti.
 */
@WebServlet("/RegistrationServlet")
public class RegistrationServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String nome = request.getParameter("nome");
        String cognome = request.getParameter("cognome");
        String email = request.getParameter("email");
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        UserDAO userDAO = new UserDAO();

        if (userDAO.doRetrieveByEmail(email) != null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email già registrata.");
            return;
        }

        Role ruoloDefault = Role.FANTALLENATORE;

        String hashedPassword = PasswordHasher.hash(password);

        User newUser = new User(nome, cognome, email, username, hashedPassword, ruoloDefault);
        try {

            userDAO.doSave(newUser);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("Registrazione avvenuta con successo.");
            response.sendRedirect("login.jsp");

        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante il salvataggio.");
        }
    }
}
