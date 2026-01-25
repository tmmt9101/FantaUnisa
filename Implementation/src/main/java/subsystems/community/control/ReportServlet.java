package subsystems.community.control;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import subsystems.access_profile.model.Role;
import subsystems.access_profile.model.User;
import subsystems.community.model.Report;
import subsystems.community.model.ReportDAO;

@WebServlet("/ReportServlet")
public class ReportServlet extends HttpServlet {

    // 1. UTENTE SEGNALA UN POST
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String postIdStr = request.getParameter("postId");
        String motivo = request.getParameter("motivo");

        if (postIdStr != null && motivo != null && !motivo.trim().isEmpty()) {
            try {
                int postId = Integer.parseInt(postIdStr);
                Report report = new Report(user.getEmail(), postId, motivo);

                ReportDAO reportDAO = new ReportDAO();
                reportDAO.doSave(report);

                // Redirect con messaggio di successo
                response.sendRedirect(request.getContextPath() + "/PostServlet?msg=ReportSent");
                return;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        response.sendRedirect(request.getContextPath() + "/PostServlet?error=ReportFailed");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null || user.getRole() != Role.GESTORE_UTENTI) {
            response.sendRedirect(request.getContextPath()+"/view/login.jsp");
            return;
        }

        ReportDAO reportDAO = new ReportDAO();
        String action = request.getParameter("action"); // "view" o "deleteReport"

        if ("deleteReport".equals(action)) {
            String reportIdStr = request.getParameter("id");
            if (reportIdStr != null) {
                reportDAO.doDelete(Integer.parseInt(reportIdStr));
            }
            response.sendRedirect(request.getContextPath()+"/ReportServlet?action=view");

        } else {
            List<Report> reports = reportDAO.doRetrieveAll();
            request.setAttribute("reports", reports);
            request.getRequestDispatcher("/view/admin_moderation.jsp").forward(request, response);
        }
    }
}