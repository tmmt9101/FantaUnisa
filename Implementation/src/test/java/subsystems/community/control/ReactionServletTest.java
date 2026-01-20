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
import subsystems.community.model.Reaction;
import subsystems.community.model.ReactionDAO;
import java.lang.reflect.Field;

public class ReactionServletTest {

    private ReactionServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;
    @Mock private ReactionDAO reactionDAO;

    private User currentUser;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new ReactionServlet();

        // Setup Utente
        currentUser = new User();
        currentUser.setEmail("m.rossi@studenti.unisa.it");

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(currentUser);
        // Il dispatcher serve per il refresh della pagina o redirect
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        // Injection del DAO via Reflection
        Field field = ReactionServlet.class.getDeclaredField("reactionDAO");
        field.setAccessible(true);
        field.set(servlet, reactionDAO);
    }

    // TC13.1: Aggiunta (Utente non ha ancora reagito) -> Like Aggiunto
    @Test
    void testTC13_1_AggiuntaReazione() throws Exception {
        // GIVEN
        when(request.getParameter("postId")).thenReturn("10");
        when(request.getParameter("tipo_reazione")).thenReturn("LIKE");

        // Simuliamo che NON esista una reazione precedente (ST1)
        when(reactionDAO.doRetrieveReaction(currentUser.getEmail(), 10)).thenReturn(null);

        // WHEN
        servlet.doPost(request, response);

        // THEN
        // Deve chiamare doSave (Aggiungere)
        verify(reactionDAO).doSave(any(Reaction.class));
        // NON deve chiamare doDelete
        verify(reactionDAO, never()).doDelete(anyString(), anyInt());
    }

    // TC13.2: Rimozione (Utente ha già reagito) -> Like Rimosso
    @Test
    void testTC13_2_RimozioneReazione() throws Exception {
        // GIVEN
        when(request.getParameter("postId")).thenReturn("10");
        when(request.getParameter("tipo_reazione")).thenReturn("LIKE");

        // Simuliamo che ESISTA già una reazione (ST2)
        Reaction existingReaction = new Reaction();
        existingReaction.setTipo("LIKE");
        when(reactionDAO.doRetrieveReaction(currentUser.getEmail(), 10)).thenReturn(existingReaction);

        // WHEN
        servlet.doPost(request, response);

        // THEN
        // Deve chiamare doDelete (Rimuovere)
        verify(reactionDAO).doDelete(currentUser.getEmail(), 10);
        // NON deve chiamare doSave
        verify(reactionDAO, never()).doSave(any(Reaction.class));
    }
}