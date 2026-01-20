package subsystems.community.control;

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
import subsystems.community.model.Comment;
import subsystems.community.model.CommentDAO;
import subsystems.community.model.Post;
import subsystems.community.model.PostDAO;
import java.lang.reflect.Field;

public class CommentServletTest {

    private CommentServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;
    @Mock private CommentDAO commentDAO;
    @Mock private PostDAO postDAO; // Necessario per verificare se il post esiste (TC12.3)

    private User currentUser;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new CommentServlet();

        // Setup Utente
        currentUser = new User();
        currentUser.setEmail("m.rossi@studenti.unisa.it");

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(currentUser);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        // Injection dei DAO via Reflection
        injectField(servlet, "commentDAO", commentDAO);
        injectField(servlet, "postDAO", postDAO);
    }

    private void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // TC12.1: Commento OK -> Successo
    @Test
    void testTC1_InserimentoValido() throws Exception {
        // GIVEN
        when(request.getParameter("testo")).thenReturn("Bel centrocampo!");
        when(request.getParameter("postId")).thenReturn("10");

        // Simuliamo che il post esista
        when(postDAO.doRetrieveById(10)).thenReturn(new Post());

        // WHEN
        servlet.doPost(request, response);

        // THEN
        verify(commentDAO).doSave(any(Comment.class)); // Deve salvare
        verify(request).setAttribute(eq("success"), contains("Commento aggiunto"));
    }

    // TC12.2: Commento Vuoto -> Errore
    @Test
    void testTC2_TestoVuoto() throws Exception {
        // GIVEN
        when(request.getParameter("testo")).thenReturn("   "); // Solo spazi
        when(request.getParameter("postId")).thenReturn("10");

        // WHEN
        servlet.doPost(request, response);

        // THEN
        verify(commentDAO, never()).doSave(any(Comment.class)); // NON deve salvare
        verify(request).setAttribute(eq("error"), contains("Il commento non può essere vuoto"));
    }

    // TC12.3: Post Non Trovato -> Errore
    @Test
    void testTC3_PostInesistente() throws Exception {
        // GIVEN
        when(request.getParameter("testo")).thenReturn("Commento valido");
        when(request.getParameter("postId")).thenReturn("999"); // ID inesistente

        // Simuliamo che il post NON esista (null)
        when(postDAO.doRetrieveById(999)).thenReturn(null);

        // WHEN
        servlet.doPost(request, response);

        // THEN
        verify(commentDAO, never()).doSave(any(Comment.class)); // NON deve salvare
        verify(request).setAttribute(eq("error"), contains("il post non esiste più"));
    }
}