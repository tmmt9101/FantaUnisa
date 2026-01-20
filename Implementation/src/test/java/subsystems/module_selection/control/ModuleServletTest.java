package subsystems.module_selection.control;

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
import subsystems.team_management.model.Player;
import subsystems.team_management.model.SquadDAO; // Usiamo solo questo
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModuleServletTest {

    private ModuleServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;
    @Mock private SquadDAO squadDAO; // Unico DAO necessario

    private User fantallenatore;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new ModuleServlet();

        fantallenatore = new User();
        fantallenatore.setEmail("m.rossi@studenti.unisa.it");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(fantallenatore);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        // Iniettiamo solo SquadDAO
        injectField(servlet, "squadDAO", squadDAO);
    }

    private void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // TC1: Successo
    @Test
    void testSceltaModuloValido_TC1() throws Exception {
        // Input valido presente in Module.java
        when(request.getParameter("modulo_selezionato")).thenReturn("3-4-3");

        // Simuliamo rosa completa
        List<Player> rosaCompleta = new ArrayList<>(Collections.nCopies(25, new Player()));
        when(squadDAO.doRetrieveByEmail(fantallenatore.getEmail())).thenReturn(rosaCompleta);

        servlet.doPost(request, response);

        // Oracolo: Verifica che SquadDAO salvi il modulo
        verify(squadDAO).saveModulo(eq("3-4-3"), eq(fantallenatore.getEmail()));
        verify(request).setAttribute(eq("success"), contains("Modulo salvato"));
    }

    // TC2: Rosa Vuota
    @Test
    void testRosaAssente_TC2() throws Exception {
        when(request.getParameter("modulo_selezionato")).thenReturn("4-4-2");

        // Rosa vuota
        when(squadDAO.doRetrieveByEmail(fantallenatore.getEmail())).thenReturn(new ArrayList<>());

        servlet.doPost(request, response);

        // Oracolo: Errore
        verify(request).setAttribute(eq("error"), contains("Devi prima completare la rosa"));
        verify(squadDAO, never()).saveModulo(anyString(), anyString());
    }

    // TC3: Modulo Invalido
    @Test
    void testModuloInvalido_TC3() throws Exception {
        // Input che Module.findById restituirà null
        when(request.getParameter("modulo_selezionato")).thenReturn("5-5-5");

        List<Player> rosaCompleta = new ArrayList<>(Collections.nCopies(25, new Player()));
        when(squadDAO.doRetrieveByEmail(fantallenatore.getEmail())).thenReturn(rosaCompleta);

        servlet.doPost(request, response);

        // Oracolo: Errore validazione
        verify(request).setAttribute(eq("error"), contains("Modulo non valido"));
        verify(squadDAO, never()).saveModulo(anyString(), anyString());
    }
}