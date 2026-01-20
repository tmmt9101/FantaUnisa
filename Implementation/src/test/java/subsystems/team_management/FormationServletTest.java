package subsystems.team_management.control;

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
import subsystems.team_management.model.FormationDAO;
import subsystems.team_management.model.Player;
import subsystems.team_management.model.SquadDAO;
import subsystems.module_selection.model.Module;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class FormationServletTest {

    private FormationServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;
    @Mock private SquadDAO squadDAO;
    @Mock private FormationDAO formationDAO;

    private User currentUser;
    private List<Player> rosaUtente;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new FormationServlet();

        currentUser = new User();
        currentUser.setEmail("m.rossi@studenti.unisa.it");

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(currentUser);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        // Injection dei DAO via Reflection
        injectField(servlet, "squadDAO", squadDAO);
        injectField(servlet, "formationDAO", formationDAO);

        // Preparazione Rosa Utente (Mock)
        rosaUtente = new ArrayList<>();
        // Creiamo giocatori sufficienti per i test con ruoli corretti
        rosaUtente.add(createPlayer(1, "P"));
        rosaUtente.add(createPlayer(2, "D")); rosaUtente.add(createPlayer(3, "D")); rosaUtente.add(createPlayer(4, "D"));
        rosaUtente.add(createPlayer(5, "C")); rosaUtente.add(createPlayer(6, "C")); rosaUtente.add(createPlayer(7, "C")); rosaUtente.add(createPlayer(8, "C"));
        rosaUtente.add(createPlayer(9, "A")); rosaUtente.add(createPlayer(10, "A")); rosaUtente.add(createPlayer(11, "A"));
        // Riserve
        rosaUtente.add(createPlayer(12, "A"));

        when(squadDAO.doRetrieveByEmail(currentUser.getEmail())).thenReturn(rosaUtente);
    }

    private Player createPlayer(int id, String ruolo) {
        Player p = new Player();
        p.setId(id);
        p.setRuolo(ruolo);
        return p;
    }

    private void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // TC14.1: Successo (Modulo 3-4-3 rispettato, 11 titolari, proprietario ok)
    @Test
    void testTC14_1_SchieramentoValido() throws Exception {
        // GIVEN
        when(request.getParameter("modulo")).thenReturn("3-4-3");
        // IDs: 1 Portiere, 3 Dif (2,3,4), 4 Cen (5,6,7,8), 3 Att (9,10,11)
        when(request.getParameterValues("titolari")).thenReturn(new String[]{"1","2","3","4","5","6","7","8","9","10","11"});
        when(request.getParameterValues("panchina")).thenReturn(new String[]{"12"});

        // WHEN
        servlet.doPost(request, response);

        // THEN
        verify(formationDAO).doSave(anyList(), anyList(), eq("3-4-3"), eq(currentUser.getEmail()));
        verify(request).setAttribute(eq("success"), contains("Formazione salvata"));
    }

    // TC14.2 / TC14.3: Errore Coerenza Modulo (Utente sceglie 4-4-2 ma schiera 3 difensori)
    @Test
    void testTC14_2_ErroreModulo() throws Exception {
        // GIVEN
        when(request.getParameter("modulo")).thenReturn("4-4-2"); // Richiederebbe 4 Difensori
        // Invio solo 3 Difensori (2,3,4) e 3 Attaccanti (9,10,11) -> Incoerente
        when(request.getParameterValues("titolari")).thenReturn(new String[]{"1","2","3","4","5","6","7","8","9","10","11"});
        when(request.getParameterValues("panchina")).thenReturn(new String[]{});

        // WHEN
        servlet.doPost(request, response);

        // THEN
        verify(formationDAO, never()).doSave(anyList(), anyList(), anyString(), anyString());
        verify(request).setAttribute(eq("error"), contains("non rispetta il modulo"));
    }

    // TC14.4: Giocatore Non Posseduto (Tentativo manipolazione ID)
    @Test
    void testTC14_4_GiocatoreNonPosseduto() throws Exception {
        // GIVEN
        when(request.getParameter("modulo")).thenReturn("3-4-3");
        // L'ID 99 non è nella rosaUtente mockata
        when(request.getParameterValues("titolari")).thenReturn(new String[]{"1","2","3","4","5","6","7","8","9","10","99"});

        // WHEN
        servlet.doPost(request, response);

        // THEN
        verify(request).setAttribute(eq("error"), contains("non fanno parte della tua rosa"));
    }

    // TC14.5: Duplicati (Stesso giocatore Titolare e Panchina)
    @Test
    void testTC14_5_Duplicati() throws Exception {
        // GIVEN
        when(request.getParameter("modulo")).thenReturn("3-4-3");
        // Giocatore 10 è sia titolare che panchinaro
        when(request.getParameterValues("titolari")).thenReturn(new String[]{"1","2","3","4","5","6","7","8","9","10","11"});
        when(request.getParameterValues("panchina")).thenReturn(new String[]{"10"});

        // WHEN
        servlet.doPost(request, response);

        // THEN
        verify(request).setAttribute(eq("error"), contains("Giocatore schierato più volte"));
    }
}