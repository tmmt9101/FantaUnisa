package subsystems.statistics_viewer.control;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import subsystems.statistics_import.model.StatisticheDAO;
import subsystems.statistics_viewer.model.Statistiche;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class LoadStatisticsServletTest {

    private LoadStatisticsServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private StatisticheDAO dao;
    @Mock private PrintWriter writer;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new LoadStatisticsServlet();

        // Simuliamo il writer per catturare l'HTML generato
        when(response.getWriter()).thenReturn(writer);

        // Injection del DAO Mockato via Reflection
        Field field = LoadStatisticsServlet.class.getDeclaredField("dao");
        field.setAccessible(true);
        field.set(servlet, dao);
    }

    // TC1: Dati Trovati -> Tabella HTML
    // Corrisponde a PDF UC8 TC1 (ma usando ID invece di Nome)
    @Test
    void testTC1_StatistichePresenti() throws Exception {
        // GIVEN: PlayerID valido e DAO restituisce dati
        when(request.getParameter("playerId")).thenReturn("10");

        List<Statistiche> mockStats = new ArrayList<>();
        Statistiche stat = new Statistiche();
        stat.setGiornata(1);
        stat.setMediaVoto(6.5f);
        mockStats.add(stat);

        when(dao.findByPlayerAndRange(10, null, null)).thenReturn(mockStats);

        // WHEN
        servlet.doGet(request, response);

        // THEN
        // Verifichiamo che venga stampata la tabella
        verify(writer).println(contains("<table border='1'>"));
        verify(writer, never()).println(contains("Nessuna statistica disponibile"));
    }

    // TC2: Nessun Dato -> Messaggio "Nessuna statistica"
    // Corrisponde a PDF UC8 TC2
    @Test
    void testTC2_StatisticheNonTrovate() throws Exception {
        // GIVEN: PlayerID valido ma DAO restituisce lista vuota
        when(request.getParameter("playerId")).thenReturn("99");
        when(dao.findByPlayerAndRange(99, null, null)).thenReturn(new ArrayList<>());

        // WHEN
        servlet.doGet(request, response);

        // THEN
        // Verifichiamo messaggio di assenza dati
        verify(writer).println(contains("Nessuna statistica disponibile"));
        verify(writer, never()).println(contains("<table border='1'>"));
    }

    // TC3: Input Invalido (Non numerico) -> Errore 400
    // Corrisponde a PDF UC8 TC3
    @Test
    void testTC3_InputNonValido() throws Exception {
        // GIVEN: playerId non numerico
        when(request.getParameter("playerId")).thenReturn("abc");

        // WHEN
        servlet.doGet(request, response);

        // THEN
        // Verifichiamo errore HTTP Bad Request
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "playerId non valido");
        verify(dao, never()).findByPlayerAndRange(anyInt(), any(), any());
    }

    // Test Extra: Input Mancante -> Errore 400
    @Test
    void testInputMancante() throws Exception {
        // GIVEN: playerId null
        when(request.getParameter("playerId")).thenReturn(null);

        // WHEN
        servlet.doGet(request, response);

        // THEN
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Parametro playerId mancante");
    }
}