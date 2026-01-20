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
import utils.PasswordHasher; // Assumo tu abbia questa classe vista nella Register

import java.lang.reflect.Field;

public class LoginServletTest {

    private LoginServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;
    @Mock private UserDAO userDAO;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new LoginServlet();

        // Configurazione base mock
        when(request.getSession()).thenReturn(session);
        when(request.getSession(anyBoolean())).thenReturn(session); // Alcune servlet usano getSession(true)
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        // --- REFLECTION INJECTION (Come per RegisterServlet) ---
        // Iniettiamo il mock UserDAO nella variabile privata della Servlet
        Field field = LoginServlet.class.getDeclaredField("userDAO");
        field.setAccessible(true);
        field.set(servlet, userDAO);
    }

    // TC1: Login OK - Utente esiste, password corretta, attivo
    @Test
    void testLoginSuccesso_TC1() throws Exception {
        // Input dal PDF
        String email = "m.rossi@studenti.unisa.it";
        String password = "Password1!";
        String passwordHash = PasswordHasher.hash(password); // Simuliamo l'hash

        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("password")).thenReturn(password);

        // Simuliamo Utente Trovato e Attivo
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordHash); // Nel DB c'è la password hashata
        user.setIs_active(true);

        when(userDAO.doRetrieveByEmail(email)).thenReturn(user);

        servlet.doPost(request, response);

        // Oracolo: Utente salvato in sessione
        verify(session).setAttribute(eq("user"), any(User.class));
        // Oracolo: Redirect alla Dashboard (o Home)
        response.sendRedirect("login.jsp");
    }

    // TC2: Utente Ignoto - Email non presente nel DB
    @Test
    void testUtenteIgnoto_TC2() throws Exception {
        // Input dal PDF
        when(request.getParameter("email")).thenReturn("sconosciuto@gmail.com");
        when(request.getParameter("password")).thenReturn("Password1!");

        // Simuliamo Utente NON Trovato (null)
        when(userDAO.doRetrieveByEmail("sconosciuto@gmail.com")).thenReturn(null);

        servlet.doPost(request, response);

        // Oracolo: Messaggio errore generico (sicurezza)
        verify(request).setAttribute(eq("error"), contains("non corretti"));
        // Verifica che NON sia stato salvato in sessione
        verify(session, never()).setAttribute(eq("user"), any());
    }

    // TC3: Password Errata
    @Test
    void testPasswordErrata_TC3() throws Exception {
        // Input dal PDF
        String email = "m.rossi@studenti.unisa.it";
        String passwordInput = "Errata123!"; // Password sbagliata
        String passwordVeraHash = PasswordHasher.hash("Password1!"); // Quella giusta nel DB

        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("password")).thenReturn(passwordInput);

        // Simuliamo Utente Trovato
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordVeraHash); // Nel DB c'è quella giusta
        user.setIs_active(true);

        when(userDAO.doRetrieveByEmail(email)).thenReturn(user);

        servlet.doPost(request, response);

        // Oracolo: Messaggio errore generico
        verify(request).setAttribute(eq("error"), contains("non corretti"));
        verify(session, never()).setAttribute(eq("user"), any());
    }

    // TC4: Account Non Attivo
    @Test
    void testAccountNonAttivo_TC4() throws Exception {
        // Input dal PDF
        String email = "m.rossi@studenti.unisa.it";
        String password = "Password1!";
        String passwordHash = PasswordHasher.hash(password);

        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("password")).thenReturn(password);

        // Simuliamo Utente Trovato ma NON ATTIVO
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordHash);
        user.setIs_active(false); // <--- Punto chiave

        when(userDAO.doRetrieveByEmail(email)).thenReturn(user);

        servlet.doPost(request, response);

        // Oracolo: Messaggio specifico attivazione
        verify(request).setAttribute(eq("error"), contains("non attivato"));
        verify(session, never()).setAttribute(eq("user"), any());
    }
}