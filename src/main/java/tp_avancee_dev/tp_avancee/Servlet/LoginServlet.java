package tp_avancee_dev.tp_avancee.Servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import tp_avancee_dev.tp_avancee.model.User;
import tp_avancee_dev.tp_avancee.service.AuthService;

import java.io.IOException;

@WebServlet(name = "loginServlet", value = "/login")
public class LoginServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String login = request.getParameter("login");
        String password = request.getParameter("password");

        if (login == null || login.isBlank() || password == null || password.isBlank()) {
            request.setAttribute("error", "Identifiants obligatoires");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        User user = authService.login(login.trim(), password);
        if (user == null) {
            request.setAttribute("error", "Login ou mot de passe invalide");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("userId", user.getId());
        session.setAttribute("username", user.getUsername());

        response.sendRedirect(request.getContextPath() + "/annonce-list");
    }
}
