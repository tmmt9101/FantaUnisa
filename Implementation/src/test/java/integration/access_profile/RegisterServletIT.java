package integration.access_profile;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import subsystems.access_profile.control.RegisterServlet;
import subsystems.access_profile.model.User;
import subsystems.access_profile.model.UserDAO;
import subsystems.access_profile.model.Role;
import connection.DBConnection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Statement;

public class RegisterServletIT {

    private RegisterServlet servlet;
    private UserDAO userDAO;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Mock del Dispatcher per evitare NullPointerException
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        // 1. SETUP H2 DATABASE
        JdbcDataSource h2DataSource = new JdbcDataSource();
        // NON_KEYWORDS=USER serve perché 'user' è una parola riservata SQL
        h2DataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER");
        h2DataSource.setUser("sa");
        h2DataSource.setPassword("");

        // 2. INIEZIONE DATABASE IN DBConnection
        Field dsField = DBConnection.class.getDeclaredField("ds");
        dsField.setAccessible(true);
        dsField.set(null, h2DataSource);

        // 3. CREAZIONE TABELLA COMPLETA
        // Questa struttura DEVE avere tutti i campi usati nel tuo UserDAO.doSave()
        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS user (" +
                    "email VARCHAR(255) PRIMARY KEY, " +
                    "password VARCHAR(255), " +
                    "nome VARCHAR(255), " +
                    "cognome VARCHAR(255), " +
                    "username VARCHAR(255), " +
                    "ruolo VARCHAR(50), " +
                    "is_active BOOLEAN, " +
                    "verification_token VARCHAR(255), " +
                    "resetToken VARCHAR(255), " +
                    "resetExpiry DATETIME)");
        }

        // 4. SETUP SERVLET E DAO
        servlet = new RegisterServlet();
        userDAO = new UserDAO();

        Field daoField = RegisterServlet.class.getDeclaredField("userDAO");
        daoField.setAccessible(true);
        daoField.set(servlet, userDAO);
    }

    @AfterEach
    void tearDown() throws Exception {
        // Pulizia DB tra un test e l'altro
        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement()) {
            stmt.execute("TRUNCATE TABLE user");
        }
    }

    private void executeServlet() throws Exception {
        Method doPost = RegisterServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);
    }

    // --- 5 TEST CASE SPECIFICI (Come da tua richiesta) ---

    // TC1: Registrazione OK
    @Test
    void testTC1_RegOK() throws Exception {
        // Input: new@test.it, pass: Secure1!pass
        when(request.getParameter("nome")).thenReturn("Mario");
        when(request.getParameter("cognome")).thenReturn("Rossi");
        when(request.getParameter("email")).thenReturn("new@test.it");
        when(request.getParameter("password")).thenReturn("Secure1!pass");
        when(request.getParameter("conferma_password")).thenReturn("Secure1!pass");
        when(request.getParameter("username")).thenReturn("mario_new"); // Campo obbligatorio DB

        executeServlet();

        // Verifica: Utente salvato nel DB
        User salvato = userDAO.doRetrieveByEmail("new@test.it");
        assertNotNull(salvato, "L'utente dovrebbe essere stato salvato (TC1)");
        assertEquals("Mario", salvato.getNome());
    }

    // TC2: Email Duplicata
    @Test
    void testTC2_EmailDup() throws Exception {
        // DB: email presente (m.rossi@unisa.it)
        User oldUser = new User();
        oldUser.setEmail("m.rossi@unisa.it");
        oldUser.setNome("Esistente");
        oldUser.setCognome("User");
        oldUser.setPassword("OldPass1!");
        oldUser.setUsername("mrossi_old");
        oldUser.setRole(Role.FANTALLENATORE);
        oldUser.setIs_active(true);
        userDAO.doSave(oldUser);

        // Input: m.rossi@unisa.it, pass: Secure1!pass
        when(request.getParameter("nome")).thenReturn("Nuovo");
        when(request.getParameter("cognome")).thenReturn("Tizio");
        when(request.getParameter("email")).thenReturn("m.rossi@unisa.it");
        when(request.getParameter("password")).thenReturn("Secure1!pass");
        when(request.getParameter("conferma_password")).thenReturn("Secure1!pass");
        when(request.getParameter("username")).thenReturn("nuovo_tizio");

        executeServlet();

        // Verifica: L'utente nel DB deve essere ancora quello vecchio
        User check = userDAO.doRetrieveByEmail("m.rossi@unisa.it");
        assertEquals("Esistente", check.getNome(), "L'utente non deve essere sovrascritto (TC2)");

        // Verifica errore
        verify(request).setAttribute(eq("error"), contains("E-mail già in uso")); // O messaggio simile
    }

    // TC3: Password Corta (< 8 caratteri)
    @Test
    void testTC3_PassCorta() throws Exception {
        // Input: new@test.it, pass: Short1!
        when(request.getParameter("nome")).thenReturn("Mario");
        when(request.getParameter("cognome")).thenReturn("Bianchi");
        when(request.getParameter("email")).thenReturn("new@test.it");
        when(request.getParameter("password")).thenReturn("Short1!");
        when(request.getParameter("conferma_password")).thenReturn("Short1!");
        when(request.getParameter("username")).thenReturn("mario_short");

        executeServlet();

        // Verifica: NON salvato nel DB
        User salvato = userDAO.doRetrieveByEmail("new@test.it");
        assertNull(salvato, "Utente salvato con password corta! (TC3 Fallito)");
        verify(request).setAttribute(eq("error"), anyString());
    }

    // TC4: Password No Symbol (Formato pass errato)
    @Test
    void testTC4_PassNoSym() throws Exception {
        // Input: new@test.it, pass: Password123
        when(request.getParameter("nome")).thenReturn("Mario");
        when(request.getParameter("cognome")).thenReturn("Verdi");
        when(request.getParameter("email")).thenReturn("new@test.it");
        when(request.getParameter("password")).thenReturn("Password123");
        when(request.getParameter("conferma_password")).thenReturn("Password123");
        when(request.getParameter("username")).thenReturn("mario_nosym");

        executeServlet();

        // Verifica: NON salvato nel DB
        User salvato = userDAO.doRetrieveByEmail("new@test.it");
        assertNull(salvato, "Utente salvato con password senza simboli! (TC4 Fallito)");
        verify(request).setAttribute(eq("error"), anyString());
    }

    // TC5: Errore Complessità (No Num/Simb)
    @Test
    void testTC5_Frame5_ErroreComplessita() throws Exception {
        // Input: marco@test.it, pass: passwordlunga
        when(request.getParameter("nome")).thenReturn("Marco");
        when(request.getParameter("cognome")).thenReturn("Neri");
        when(request.getParameter("email")).thenReturn("marco@test.it");
        when(request.getParameter("password")).thenReturn("passwordlunga");
        when(request.getParameter("conferma_password")).thenReturn("passwordlunga");
        when(request.getParameter("username")).thenReturn("marco_user");

        executeServlet();

        // Verifica: NON salvato nel DB
        User salvato = userDAO.doRetrieveByEmail("marco@test.it");
        assertNull(salvato, "Utente salvato con password debole! (TC5 Fallito)");
        verify(request).setAttribute(eq("error"), anyString());
    }
}