package utils;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import jakarta.servlet.Filter;
import subsystems.access_profile.model.Role;
import subsystems.access_profile.model.User;

@WebFilter("/*")
public class AuthFilter implements Filter {

    // 1. RISORSE PUBBLICHE (Accessibili a TUTTI)
    private static final String[] PUBLIC_URLS = {
            "/index.jsp",
            "/login.jsp",
            "/LoginServlet",
            "/RegisterServlet",
            "/ActivationServlet",
            "/ResetPasswordServlet",
            "/css/", "/js/", "/images/", "/styles/", "/scripts/"
    };

    // 2. RISORSE GESTORE DATI
    private static final String[] DATA_ADMIN_URLS = {
            "/view/admin_upload.jsp",
            "/StatisticsImportServlet"
    };

    // 3. RISORSE GESTORE UTENTI
    private static final String[] USER_ADMIN_URLS = {
            "/view/admin_moderation.jsp",
            "/BanUserServlet",      // Se esiste
            "/DeleteContentServlet" // Se esiste
    };

    // 4. RISORSE FANTALLENATORE
    private static final String[] GAME_URLS = {
            "/view/rosa.jsp",
            "/view/formazione.jsp",
            "/SquadServlet",
            "/FormationServlet",
            "/ModuleServlet",
            "/CommentServlet",
            "/ReactionServlet",
            "ProfileServlet",
    };

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession(false);

        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();


        for (String publicUrl : PUBLIC_URLS) {
            if (uri.contains(publicUrl)) {
                chain.doFilter(request, response);
                return;
            }
        }


        boolean isLoggedIn = (session != null && session.getAttribute("user") != null);
        if (!isLoggedIn) {
            response.sendRedirect(contextPath + "/view/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");
        Role role = user.getRole();


        for (String url : DATA_ADMIN_URLS) {
            if (uri.contains(url)) {
                if (!role.equals(Role.GESTORE_DATI)) {
                    response.sendRedirect(contextPath + "/view/index.jsp");
                    return;
                }
            }
        }


        for (String url : USER_ADMIN_URLS) {
            if (uri.contains(url)) {
                if (!role.equals(Role.GESTORE_UTENTI)) {
                    response.sendRedirect(contextPath + "/view/index.jsp");
                    return;
                }
            }
        }


        for (String url : GAME_URLS) {
            if (uri.contains(url)) {
                if (!role.equals(Role.FANTALLENATORE)) {
                    System.out.println("Admin " + user.getEmail() + " ha tentato di accedere al gioco.");

                    if(role.equals(Role.GESTORE_DATI)) {
                        response.sendRedirect(contextPath + "/view/admin_upload.jsp");
                    } else if (role.equals(Role.GESTORE_UTENTI)) {
                        response.sendRedirect(contextPath + "/view/admin_moderation.jsp");
                    } else {
                        response.sendRedirect(contextPath + "/view/index.jsp");
                    }
                    return;
                }
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}