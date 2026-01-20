package subsystems.community.control;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import subsystems.access_profile.model.User;
import subsystems.community.model.Post;
import subsystems.community.model.PostDAO;
import java.lang.reflect.Field;

public class PostServletTest {

    private PostServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;
    @Mock private PostDAO postDAO;

    private User currentUser;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new PostServlet();

        // Setup Utente loggato
        currentUser = new User();
        currentUser.setEmail("m.rossi@studenti.unisa.it");

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(currentUser);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        // Injection del DAO Mockato via Reflection
        Field field = PostServlet.class.getDeclaredField("postDAO");
        field.setAccessible(true);
        field.set(servlet, postDAO);
    }

    // --- CREAZIONE POST ---

    // TC1: Solo Testo ("Ciao"), Allegato Null -> Pubblicato
    @Test
    void testTC1_SoloTesto() throws Exception {
        // GIVEN
        when(request.getParameter("action")).thenReturn("create");
        when(request.getParameter("testo")).thenReturn("Ciao a tutti");
        when(request.getParameter("formationId")).thenReturn(null); // Nessuna formazione

        // WHEN
        servlet.doPost(request, response);

        // THEN
        // Catturiamo l'oggetto passato al DAO per verificare i campi
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postDAO).doSave(postCaptor.capture());

        Post capturedPost = postCaptor.getValue();
        assertEquals("Ciao a tutti", capturedPost.getTesto());
        assertNull(capturedPost.getFormationId());

        verify(request).setAttribute(eq("success"), contains("Post pubblicato"));
    }

    // TC2: Testo Null (o vuoto), Allegato Formazione -> Pubblicato
    @Test
    void testTC2_SoloAllegato() throws Exception {
        // GIVEN
        when(request.getParameter("action")).thenReturn("create");
        when(request.getParameter("testo")).thenReturn("");
        when(request.getParameter("formationId")).thenReturn("123"); // ID formazione valido

        // WHEN
        servlet.doPost(request, response);

        // THEN
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postDAO).doSave(postCaptor.capture());

        Post capturedPost = postCaptor.getValue();
        assertEquals(123, capturedPost.getFormationId()); // Verifica conversione Integer

        verify(request).setAttribute(eq("success"), contains("Post pubblicato"));
    }

    // TC3: Testo e Allegato presenti -> Pubblicato
    @Test
    void testTC3_Completo() throws Exception {
        // GIVEN
        when(request.getParameter("action")).thenReturn("create");
        when(request.getParameter("testo")).thenReturn("Ecco la mia rosa");
        when(request.getParameter("formationId")).thenReturn("55");

        // WHEN
        servlet.doPost(request, response);

        // THEN
        verify(postDAO).doSave(any(Post.class));
        verify(request).setAttribute(eq("success"), contains("Post pubblicato"));
    }

    // TC4: Testo Null, Allegato Null -> Errore Vuoto
    @Test
    void testTC4_Vuoto() throws Exception {
        // GIVEN
        when(request.getParameter("action")).thenReturn("create");
        when(request.getParameter("testo")).thenReturn("");
        when(request.getParameter("formationId")).thenReturn(null);

        // WHEN
        servlet.doPost(request, response);

        // THEN
        verify(postDAO, never()).doSave(any(Post.class)); // Il DAO non deve essere chiamato
        verify(request).setAttribute(eq("error"), contains("Il post non può essere vuoto"));
    }

    // --- ELIMINAZIONE POST ---

    // TC5: Elimina post proprio -> Eliminato
    @Test
    void testTC5_EliminaProprio() throws Exception {
        // GIVEN
        when(request.getParameter("action")).thenReturn("delete");
        when(request.getParameter("postId")).thenReturn("10");

        // Simuliamo che il post esista e appartenga all'utente corrente
        Post post = new Post();
        post.setId(10);
        post.setUserEmail(currentUser.getEmail()); // Autore = Current User
        when(postDAO.doRetrieveById(10)).thenReturn(post);

        // WHEN
        servlet.doPost(request, response);

        // THEN
        verify(postDAO).doDelete(10);
        verify(request).setAttribute(eq("success"), contains("Post eliminato"));
    }

    // TC6: Elimina post altrui -> Errore Permessi
    @Test
    void testTC6_EliminaAltrui() throws Exception {
        // GIVEN
        when(request.getParameter("action")).thenReturn("delete");
        when(request.getParameter("postId")).thenReturn("20");

        // Simuliamo che il post appartenga a un altro utente
        Post post = new Post();
        post.setId(20);
        post.setUserEmail("altro.utente@test.it"); // Autore DIVERSO
        when(postDAO.doRetrieveById(20)).thenReturn(post);

        // WHEN
        servlet.doPost(request, response);

        // THEN
        verify(postDAO, never()).doDelete(20); // NON deve cancellare
        verify(request).setAttribute(eq("error"), contains("Non hai i permessi"));
    }
}