package subsystems.access_profile.control;

import static org.mockito.ArgumentMatchers.any;
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

public class RegisterServletTest {

    private RegisterServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;

    @Mock private UserDAO userDAO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        servlet = new RegisterServlet();


        servlet.setUserDAO(userDAO);


        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    @Test
    void testRegistrazioneValida_TC1() throws Exception {

        when(request.getParameter("nome")).thenReturn("Marco");
        when(request.getParameter("cognome")).thenReturn("Rossi");
        when(request.getParameter("email")).thenReturn("new@test.it");
        when(request.getParameter("password")).thenReturn("Secure1!");
        when(request.getParameter("username")).thenReturn("marcorossi");


        when(userDAO.doRetrieveByEmail("new@test.it")).thenReturn(null);


        servlet.doPost(request, response);


        verify(userDAO).doSave(any(User.class));

        verify(response).sendRedirect("registrazione_successo.jsp");
    }

    @Test
    void testEmailGiaPresente_TC2() throws Exception {

        when(request.getParameter("email")).thenReturn("m.rossi@unisa.it");
        when(request.getParameter("password")).thenReturn("Secure1!");

        when(userDAO.doRetrieveByEmail("m.rossi@unisa.it")).thenReturn(new User());

        servlet.doPost(request, response);


        verify(request).setAttribute(eq("error"), contains("E-mail già in uso"));

        verify(userDAO, never()).doSave(any(User.class));
    }

    @Test
    void testPasswordCorta_TC3() throws Exception {

        when(request.getParameter("email")).thenReturn("new@test.it");
        when(request.getParameter("password")).thenReturn("Short1!");


        when(userDAO.doRetrieveByEmail("new@test.it")).thenReturn(null);

        servlet.doPost(request, response);


        verify(request).setAttribute(eq("error"), contains("almeno 8 caratteri"));
        verify(userDAO, never()).doSave(any(User.class));
    }

    @Test
    void testPasswordSenzaSimboli_TC4() throws Exception {

        when(request.getParameter("email")).thenReturn("new@test.it");
        when(request.getParameter("password")).thenReturn("Password123"); // Ha numeri ma niente simboli

        when(userDAO.doRetrieveByEmail("new@test.it")).thenReturn(null);

        servlet.doPost(request, response);


        verify(request).setAttribute(eq("error"), contains("un numero e un simbolo"));
        verify(userDAO, never()).doSave(any(User.class));
    }
    @Test
    void testPasswordSenzaNumeriNeSimboli_TC5() throws Exception {
        // Input precisi dal PDF
        when(request.getParameter("nome")).thenReturn("Marco");
        when(request.getParameter("email")).thenReturn("marco@test.it");
        when(request.getParameter("password")).thenReturn("passwordlunga"); // Solo lettere
        when(request.getParameter("conferma_password")).thenReturn("passwordlunga");


        when(userDAO.doRetrieveByEmail("marco@test.it")).thenReturn(null);

        servlet.doPost(request, response);


        verify(request).setAttribute(eq("error"), contains("un numero e un simbolo"));


        verify(userDAO, never()).doSave(any(User.class));
    }
}