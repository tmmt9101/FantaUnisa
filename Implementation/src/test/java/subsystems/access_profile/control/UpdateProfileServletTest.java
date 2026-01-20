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
import subsystems.access_profile.model.User;
import subsystems.access_profile.model.UserDAO;
import java.lang.reflect.Field;

public class UpdateProfileServletTest {

    private UpdateProfileServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;
    @Mock private UserDAO userDAO;

    private User currentUser;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new UpdateProfileServlet();

        // Setup Utente loggato
        currentUser = new User();
        currentUser.setEmail("vecchia@test.it");
        currentUser.setPassword("VecchiaPass1!");

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(currentUser);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        // Injection del DAO Mockato via Reflection
        Field field = UpdateProfileServlet.class.getDeclaredField("userDAO");
        field.setAccessible(true);
        field.set(servlet, userDAO);
    }

    // TC1: Nuova email valida, pass vuota -> Email Aggiornata
    @Test
    void testTC1_SoloEmail() throws Exception {
        // GIVEN
        when(request.getParameter("nuova_email")).thenReturn("nuova@test.it");
        when(request.getParameter("nuova_password")).thenReturn(""); // Vuota
        when(request.getParameter("conferma_password")).thenReturn("");

        // Simuliamo che la nuova email NON esista già (doRetrieve restituisce null)
        when(userDAO.doRetrieveByEmail("nuova@test.it")).thenReturn(null);

        // WHEN
        servlet.doPost(request, response);

        // THEN
        verify(userDAO).doUpdateEmail("vecchia@test.it", "nuova@test.it"); // Aggiorna Email
        verify(userDAO, never()).doUpdatePassword(anyString(), anyString()); // NON tocca password
        verify(request).setAttribute(eq("success"), contains("Profilo aggiornato"));
    }

    // TC2: Email vuota, pass "New1!", conf "New1!" -> Pass Aggiornata
    @Test
    void testTC2_SoloPassword() throws Exception {
        // GIVEN
        when(request.getParameter("nuova_email")).thenReturn(""); // Vuota
        when(request.getParameter("nuova_password")).thenReturn("NewPass1!");
        when(request.getParameter("conferma_password")).thenReturn("NewPass1!");

        // WHEN
        servlet.doPost(request, response);

        // THEN
        verify(userDAO, never()).doUpdateEmail(anyString(), anyString()); // NON tocca email
        verify(userDAO).doUpdatePassword("vecchia@test.it", "NewPass1!"); // Aggiorna Password
        verify(request).setAttribute(eq("success"), contains("Profilo aggiornato"));
    }

    // TC3: Nuova email, pass e conferma ok -> Tutto Aggiornato
    @Test
    void testTC3_EmailEPassword() throws Exception {
        // GIVEN
        when(request.getParameter("nuova_email")).thenReturn("nuova@test.it");
        when(request.getParameter("nuova_password")).thenReturn("NewPass1!");
        when(request.getParameter("conferma_password")).thenReturn("NewPass1!");
        when(userDAO.doRetrieveByEmail("nuova@test.it")).thenReturn(null); // Email libera

        // WHEN
        servlet.doPost(request, response);

        // THEN
        verify(userDAO).doUpdateEmail("vecchia@test.it", "nuova@test.it");
        verify(userDAO).doUpdatePassword(anyString(), eq("NewPass1!")); // Nota: user.getEmail sarà aggiornata
    }

    // TC4: Nuova email "errata" o già in uso -> Errore Email
    @Test
    void testTC4_EmailGiaInUso() throws Exception {
        // GIVEN
        when(request.getParameter("nuova_email")).thenReturn("occupata@test.it");
        when(request.getParameter("nuova_password")).thenReturn("");

        // Simuliamo che l'email esista già (ritorna un utente diverso)
        when(userDAO.doRetrieveByEmail("occupata@test.it")).thenReturn(new User());

        // WHEN
        servlet.doPost(request, response);

        // THEN
        verify(userDAO, never()).doUpdateEmail(anyString(), anyString());
        verify(request).setAttribute(eq("error"), contains("Email già in uso"));
    }

    // TC5: Email vuota, pass "New1!", conf "Diversa!" -> Errore Conferma
    @Test
    void testTC5_ConfermaErrata() throws Exception {
        // GIVEN
        when(request.getParameter("nuova_email")).thenReturn("");
        when(request.getParameter("nuova_password")).thenReturn("NewPass1!");
        when(request.getParameter("conferma_password")).thenReturn("Diversa!"); // Mismatch

        // WHEN
        servlet.doPost(request, response);

        // THEN
        verify(userDAO, never()).doUpdatePassword(anyString(), anyString());
        verify(request).setAttribute(eq("error"), contains("Le password non coincidono"));
    }
}