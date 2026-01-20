package subsystems.access_profile.control;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import subsystems.access_profile.model.Role;
import subsystems.access_profile.model.User;
import subsystems.access_profile.model.UserDAO;
import java.lang.reflect.Field;

public class DeleteUserServletTest {

    private DeleteUserServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private UserDAO userDAO;

    private User standardUser;
    private User adminUser;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new DeleteUserServlet();

        // Setup Utente Standard (Per TC1)
        standardUser = new User();
        standardUser.setEmail("user@test.com");
        standardUser.setRole(Role.FANTALLENATORE);

        // Setup Utente Admin (Per logica extra servlet)
        adminUser = new User();
        adminUser.setEmail("admin@test.com");
        adminUser.setRole(Role.GESTORE_UTENTI);

        // Injection del DAO Mockato via Reflection
        // Questo è il trucco per testare senza cambiare la logica interna
        Field field = DeleteUserServlet.class.getDeclaredField("userDAO");
        field.setAccessible(true);
        field.set(servlet, userDAO);
    }


    @Test
    void testTC1_SelfDelete() throws Exception {
        // GIVEN: Utente loggato, parametro email null (quindi cancella se stesso)
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(standardUser);
        when(request.getParameter("email")).thenReturn(null);

        // WHEN
        servlet.doPost(request, response);

        // THEN
        // 1. Verifica cancellazione DB
        verify(userDAO).doDelete("user@test.com");

        // 2. Verifica Logout Forzato (Oracolo TC1)
        verify(session).invalidate();

        // 3. Verifica Redirect
        verify(response).sendRedirect(contains("login.jsp"));
    }


    @Test
    void testTC3_SessionExpired() throws Exception {
        // GIVEN: Sessione esiste ma l'attributo user è null
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(null);

        // WHEN
        servlet.doPost(request, response);

        // THEN
        verify(userDAO, never()).doDelete(anyString()); // Nessuna azione sul DB
        verify(response).sendRedirect("login.jsp"); // Redirect Login
    }


    @Test
    void testAdminDelete_User() throws Exception {
        // GIVEN: Admin loggato cancella un altro utente
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(adminUser);
        when(request.getParameter("email")).thenReturn("victim@test.com");

        // WHEN
        servlet.doPost(request, response);

        // THEN
        verify(userDAO).doDelete("victim@test.com");
        verify(session, never()).invalidate(); // Admin resta loggato
        verify(response).sendRedirect(contains("admin/gestione_utenti.jsp"));
    }
}