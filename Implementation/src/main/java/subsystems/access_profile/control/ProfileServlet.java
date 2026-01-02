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

/**
 * Descrizione: Gestisce la visualizzazione dei dati del profilo.
 */
@WebServlet("/ProfileServlet")
public class ProfileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Verifica Pre-condizioni: L'utente deve possedere un token di sessione valido.
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        User sessionUser = (User) session.getAttribute("user");

        // 2. Post-condizioni: Vengono caricati i dati anagrafici.
        UserDAO userDAO = new UserDAO();
        User updatedUser = userDAO.doRetrieveByEmail(sessionUser.getEmail());

        if (updatedUser != null) {
            // Aggiorno l'oggetto in request scope per la visualizzazione
            request.setAttribute("userProfile", updatedUser);

            // Opzionale: Aggiorno anche la sessione se i dati fossero cambiati
            session.setAttribute("user", updatedUser);

            request.getRequestDispatcher("profile.jsp").forward(request, response);
        } else {
            session.invalidate();
            response.sendRedirect("login.jsp?error=UserNotFound");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}

