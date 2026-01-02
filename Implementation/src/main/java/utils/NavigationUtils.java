package utils;

import java.io.IOException;
import jakarta.servlet.http.HttpServletResponse;
import subsystems.access_profile.model.Role;

/**
 * Classe di utilità per gestire il routing e i reindirizzamenti in base ai ruoli.
 */
public class NavigationUtils {

    /**
     * Reindirizza l'utente alla pagina appropriata in base al suo ruolo.
     * * @param role Il ruolo dell'utente loggato
     * @param response L'oggetto HttpServletResponse necessario per il sendRedirect
     * @throws IOException Se il reindirizzamento fallisce
     */
    public static void redirectBasedOnRole(Role role, HttpServletResponse response) throws IOException {
        if (role == null) {
            response.sendRedirect("login.jsp"); // Fallback di sicurezza
            return;
        }

        switch (role) {
            case FANTALLENATORE:
                response.sendRedirect("home.jsp");
                break;

            case GESTORE_UTENTI:
                response.sendRedirect("dashboard.jsp");
                break;

            case GESTORE_DATI:
                response.sendRedirect("data_management.jsp");
                break;
        }
    }
}
