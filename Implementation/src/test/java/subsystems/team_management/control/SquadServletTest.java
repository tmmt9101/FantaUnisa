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
import subsystems.team_management.model.Player;
import subsystems.team_management.model.SquadDAO;
import subsystems.team_management.model.PlayerDAO;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class SquadServletTest {

    private SquadServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private HttpSession session;
    @Mock private RequestDispatcher dispatcher;
    @Mock private SquadDAO squadDAO;
    @Mock private PlayerDAO playerDAO;

    private User fantallenatore;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new SquadServlet();

        // Setup Sessione Utente
        fantallenatore = new User();
        fantallenatore.setEmail("m.rossi@studenti.unisa.it");
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(fantallenatore);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        // --- REFLECTION INJECTION ---
        injectField(servlet, "squadDAO", squadDAO);
        injectField(servlet, "playerDAO", playerDAO);
    }

    private void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // TC1: Rosa Valida (3P, 8D, 8C, 6A) -> Successo
    @Test
    void testSalvataggioRosaValida_TC1() throws Exception {
        // Genera 25 ID validi
        List<String> ids = generaListaID(3, 8, 8, 6);
        String[] idsArray = ids.toArray(new String[0]);

        when(request.getParameterValues("giocatoriSelezionati")).thenReturn(idsArray);

        // Mock comportamento PlayerDAO per restituire i ruoli corretti
        mockPlayerRetrieval(ids);

        servlet.doPost(request, response);

        // --- CORREZIONE QUI ---
        // Verifichiamo che doSave sia chiamato con DUE parametri: (lista, email)
        verify(squadDAO).doSave(anyList(), anyString());

        verify(request).setAttribute(eq("success"), contains("Squadra salvata con successo"));
    }

    // TC2: Rosa Incompleta (es. 24 giocatori) -> Errore
    @Test
    void testRosaIncompleta_TC2() throws Exception {
        // Genera solo 24 giocatori (manca 1 portiere)
        List<String> ids = generaListaID(2, 8, 8, 6);
        String[] idsArray = ids.toArray(new String[0]);

        when(request.getParameterValues("giocatoriSelezionati")).thenReturn(idsArray);

        servlet.doPost(request, response);

        // Oracolo: Errore numero
        verify(request).setAttribute(eq("error"), contains("Rosa incompleta - devi selezionare 25 giocatori"));

        // Verifica che NON salvi (passiamo 2 parametri anche qui per sicurezza nel match)
        verify(squadDAO, never()).doSave(anyList(), anyString());
    }

    // TC3: Ruoli Errati (es. 9 Difensori) -> Errore
    @Test
    void testRuoliErrati_TC3() throws Exception {
        // 9 Difensori e 7 Centrocampisti (Totale 25 ma sbagliati)
        List<String> ids = generaListaID(3, 9, 7, 6);
        String[] idsArray = ids.toArray(new String[0]);

        when(request.getParameterValues("giocatoriSelezionati")).thenReturn(idsArray);
        mockPlayerRetrieval(ids);

        servlet.doPost(request, response);

        // Oracolo: Errore Ruoli
        verify(request).setAttribute(eq("error"), contains("Numero massimo per ruolo raggiunto"));
        verify(squadDAO, never()).doSave(anyList(), anyString());
    }

    // TC4: Duplicati -> Errore
    @Test
    void testGiocatoriDuplicati_TC4() throws Exception {
        List<String> ids = generaListaID(3, 8, 8, 6);
        // Sostituisci l'ultimo attaccante con il primo portiere (ID duplicato)
        ids.set(24, ids.get(0));
        String[] idsArray = ids.toArray(new String[0]);

        when(request.getParameterValues("giocatoriSelezionati")).thenReturn(idsArray);

        servlet.doPost(request, response);

        // Oracolo: Errore Duplicati
        verify(request).setAttribute(eq("error"), contains("Giocatore già inserito"));
        verify(squadDAO, never()).doSave(anyList(), anyString());
    }

    // --- HELPER METHODS ---
    private List<String> generaListaID(int p, int d, int c, int a) {
        List<String> list = new ArrayList<>();
        // Genera ID fittizi tipo 101, 201, 301, 401 per distinguere i ruoli dall'ID
        for(int i=0; i<p; i++) list.add(String.valueOf(100 + i)); // 1xx = Portieri
        for(int i=0; i<d; i++) list.add(String.valueOf(200 + i)); // 2xx = Difensori
        for(int i=0; i<c; i++) list.add(String.valueOf(300 + i)); // 3xx = Centrocampisti
        for(int i=0; i<a; i++) list.add(String.valueOf(400 + i)); // 4xx = Attaccanti
        return list;
    }

    private void mockPlayerRetrieval(List<String> ids) {
        for (String idStr : ids) {
            int id = Integer.parseInt(idStr);
            Player p = new Player();
            p.setId(id);

            // Assegna ruolo in base al range dell'ID
            if (id >= 100 && id < 200) p.setRuolo("Portiere");
            else if (id >= 200 && id < 300) p.setRuolo("Difensore");
            else if (id >= 300 && id < 400) p.setRuolo("Centrocampista");
            else if (id >= 400) p.setRuolo("Attaccante");

            when(playerDAO.doRetrieveById(id)).thenReturn(p);
        }
    }
}