package subsystems.access_profile.control;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class LogoutServletTest {

    private LogoutServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new LogoutServlet();
    }

    // TC1: Utente Loggato clicca Logout -> Sessione Chiusa
    @Test
    void testTC1_LogoutSuccesso() throws Exception {
        // GIVEN: Esiste una sessione attiva
        when(request.getSession(false)).thenReturn(session);

        // WHEN
        servlet.doGet(request, response);

        // THEN
        // 1. Verifica che la sessione sia stata invalidata (Logout effettivo)
        verify(session).invalidate();

        // 2. Verifica redirect al login con messaggio di successo (come da tuo codice)
        verify(response).sendRedirect(contains("login.jsp?msg=LogoutSuccess"));
    }

    // TC2: Utente non loggato chiama URL logout -> Redirect Login
    @Test
    void testTC2_LogoutUtenteGiaDisconnesso() throws Exception {
        // GIVEN: Nessuna sessione attiva (restituisce null)
        when(request.getSession(false)).thenReturn(null);

        // WHEN
        servlet.doGet(request, response);

        // THEN
        // 1. Verifica che NON venga chiamato invalidate (evita NullPointerException)
        // Poiché session è null, il metodo invalidate() non può essere chiamato su di esso.

        // 2. Verifica redirect comunque al login
        verify(response).sendRedirect(contains("login.jsp?msg=LogoutSuccess"));
    }
}