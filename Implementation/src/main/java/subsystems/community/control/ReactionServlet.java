package subsystems.community.control;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import subsystems.access_profile.model.User;
import subsystems.community.model.Reaction;
import subsystems.community.model.ReactionDAO;

@WebServlet("/ReactionServlet")
public class ReactionServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");

        String postIdStr = request.getParameter("postId");
        String reactionType = request.getParameter("tipo"); // "LIKE", "DISLIKE"

        if (postIdStr == null || reactionType == null) {
            response.sendRedirect("community.jsp?error=InvalidReaction");
            return;
        }

        try {
            int postId = Integer.parseInt(postIdStr);
            ReactionDAO dao = new ReactionDAO();

            // 3. Logica Toggle: Controlliamo cosa aveva votato prima
            String currentReaction = dao.doRetrieveUserReaction(user.getEmail(), postId);

            if (currentReaction != null && currentReaction.equals(reactionType)) {
                // A. Se clicco la stessa cosa (es. LIKE su LIKE) -> RIMUOVO (Toggle Off)
                dao.doDelete(user.getEmail(), postId);
            } else {
                // B. Se non c'era nulla OPPURE era diverso (es. DISLIKE su LIKE) -> SALVO/AGGIORNO
                Reaction reaction = new Reaction(user.getEmail(), postId, reactionType);
                dao.doSaveOrUpdate(reaction);
            }

            // 4. Redirect alla pagina corretta (mantiene la posizione scroll se possibile)
            response.sendRedirect("PostServlet#" + postId);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("community.jsp?error=ServerReaction");
        }
    }
}