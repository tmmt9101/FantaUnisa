package subsystems.team_management.control;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import subsystems.team_management.model.Player;
import subsystems.team_management.model.PlayerDAO;

@WebServlet("/PlayerServlet")
public class PlayerServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("view/login.jsp");
            return;
        }

        String ruolo = request.getParameter("ruolo");
        String squadra = request.getParameter("squadra");

        PlayerDAO playerDAO = new PlayerDAO();
        List<Player> players = playerDAO.doRetrieveByFilter(ruolo, squadra);


        request.setAttribute("players", players);


        request.setAttribute("selectedRuolo", ruolo);
        request.setAttribute("selectedSquadra", squadra);

        request.getRequestDispatcher("view/listone_giocatori.jsp").forward(request, response);
    }
}