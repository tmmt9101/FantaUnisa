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
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import subsystems.access_profile.control.LoginServlet;
import subsystems.access_profile.model.User;
import subsystems.access_profile.model.UserDAO;
import subsystems.access_profile.model.Role;
import connection.DBConnection;
import utils.NavigationUtils;
import utils.PasswordHasher;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Statement;

public class LoginServletIT {

    private LoginServlet servlet;
    private UserDAO userDAO;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    // Costanti
    private final String EMAIL_OK = "m.rossi@studenti.unisa.it";
    private final String PWD_PLAIN = "Password1!";
    private final String MOCKED_HASH = "HASH_SICURO_123";

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Mock base
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        when(request.getSession()).thenReturn(session);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(null);

        // 1. SETUP H2 DATABASE
        JdbcDataSource h2DataSource = new JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER");
        h2DataSource.setUser("sa");
        h2DataSource.setPassword("");

        // 2. INIEZIONE DATABASE
        Field dsField = DBConnection.class.getDeclaredField("ds");
        dsField.setAccessible(true);
        dsField.set(null, h2DataSource);

        // 3. CREAZIONE TABELLA
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

        // 4. SETUP SERVLET CON SPY SUL DAO
        servlet = new LoginServlet();
        // Usiamo uno SPY invece dell'oggetto reale.
        // Ci permette di usare i metodi reali del DAO ma di sovrascriverne alcuni se necessario.
        userDAO = spy(new UserDAO());

        Field daoField = LoginServlet.class.getDeclaredField("userDAO");
        daoField.setAccessible(true);
        daoField.set(servlet, userDAO);
    }

    @AfterEach
    void tearDown() throws Exception {
        try (Connection con = DBConnection.getConnection();
             Statement stmt = con.createStatement()) {
            stmt.execute("TRUNCATE TABLE user");
        }
    }

    private void executeServlet() throws Exception {
        Method doPost = LoginServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);
    }

    // --- TEST CASE ---

    // TC1: Login OK
    @Test
    void testTC1_LoginOK() throws Exception {
        try (MockedStatic<PasswordHasher> hasherMock = mockStatic(PasswordHasher.class);
             MockedStatic<NavigationUtils> navMock = mockStatic(NavigationUtils.class)) {

            hasherMock.when(() -> PasswordHasher.hash(anyString())).thenReturn(MOCKED_HASH);

            // 1. Creiamo l'utente corretto localmente
            User u = new User();
            u.setEmail(EMAIL_OK);
            u.setPassword(MOCKED_HASH);
            u.setNome("Mario");
            u.setCognome("Rossi");
            u.setUsername("mrossi");
            u.setRole(Role.FANTALLENATORE);
            u.setIs_active(true); // È ATTIVO!

            // 2. FORZIAMO IL DAO A RESTITUIRE QUESTO OGGETTO
            // Questo bypassa il bug del tuo UserDAO che non legge il campo is_active dal DB
            doReturn(u).when(userDAO).doRetrieveByEmail(EMAIL_OK);

            // 3. Input
            when(request.getParameter("email")).thenReturn(EMAIL_OK);
            when(request.getParameter("password")).thenReturn(PWD_PLAIN);

            // 4. Esecuzione
            executeServlet();

            // 5. Verifica
            verify(session).setAttribute(eq("user"), any(User.class));
            // Verifica redirect
            navMock.verify(() -> NavigationUtils.redirectBasedOnRole(eq(Role.FANTALLENATORE), any(HttpServletResponse.class)));
        }
    }

    // TC2: Utente Ignoto
    @Test
    void testTC2_UtenteIgnoto() throws Exception {
        try (MockedStatic<PasswordHasher> hasherMock = mockStatic(PasswordHasher.class)) {
            hasherMock.when(() -> PasswordHasher.hash(anyString())).thenReturn(MOCKED_HASH);

            // Qui non serve forzare il DAO, perché doRetrieveByEmail tornerà null (corretto)
            // se l'utente non c'è, oppure possiamo forzare null per sicurezza.
            doReturn(null).when(userDAO).doRetrieveByEmail("sconosciuto@gmail.com");

            when(request.getParameter("email")).thenReturn("sconosciuto@gmail.com");
            when(request.getParameter("password")).thenReturn(PWD_PLAIN);

            executeServlet();

            verify(session, never()).setAttribute(eq("user"), any(User.class));
            verify(request).setAttribute(eq("error"), contains("E-mail o password non corretti"));
        }
    }

    // TC3: Password Errata
    @Test
    void testTC3_PasswordErrata() throws Exception {
        try (MockedStatic<PasswordHasher> hasherMock = mockStatic(PasswordHasher.class)) {
            // Simuliamo Hash diverso
            hasherMock.when(() -> PasswordHasher.hash("Errata123!")).thenReturn("HASH_SBAGLIATO");

            User u = new User();
            u.setEmail(EMAIL_OK);
            u.setPassword(MOCKED_HASH);
            u.setRole(Role.FANTALLENATORE);
            u.setIs_active(true);

            // Forziamo il ritorno dell'utente (anche qui, per coerenza)
            doReturn(u).when(userDAO).doRetrieveByEmail(EMAIL_OK);

            when(request.getParameter("email")).thenReturn(EMAIL_OK);
            when(request.getParameter("password")).thenReturn("Errata123!");

            executeServlet();

            verify(session, never()).setAttribute(eq("user"), any(User.class));
            verify(request).setAttribute(eq("error"), contains("E-mail o password non corretti"));
        }
    }

    // TC4: Account Non Attivo
    @Test
    void testTC4_NonAttivo() throws Exception {
        try (MockedStatic<PasswordHasher> hasherMock = mockStatic(PasswordHasher.class)) {
            hasherMock.when(() -> PasswordHasher.hash(anyString())).thenReturn(MOCKED_HASH);

            // Creiamo un utente NON ATTIVO
            User u = new User();
            u.setEmail(EMAIL_OK);
            u.setPassword(MOCKED_HASH);
            u.setRole(Role.FANTALLENATORE);
            u.setIs_active(false); // <--- NON ATTIVO

            // Forziamo il DAO a restituire l'utente non attivo
            doReturn(u).when(userDAO).doRetrieveByEmail(EMAIL_OK);

            when(request.getParameter("email")).thenReturn(EMAIL_OK);
            when(request.getParameter("password")).thenReturn(PWD_PLAIN);

            executeServlet();

            verify(session, never()).setAttribute(eq("user"), any(User.class));
            verify(request).setAttribute(eq("error"), contains("Account non attivato"));
        }
    }
}