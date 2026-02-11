package tp_avancee_dev.tp_avancee.Servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import tp_avancee_dev.tp_avancee.model.User;
import tp_avancee_dev.tp_avancee.service.AuthService;
import java.util.LinkedHashMap;
import java.util.Map;
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
        String login = normalize(request.getParameter("login"));
        String password = request.getParameter("password");

        Map<String, String> errors = validate(login, password);
        if (!errors.isEmpty()) {
            forwardWithErrors(request, response, errors, login);
            return;
        }

        User user = authService.login(login, password);
        if (user == null) {
            errors.put("global", "Login ou mot de passe invalide.");
            forwardWithErrors(request, response, errors, login);
            return;
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("userId", user.getId());
        session.setAttribute("username", user.getUsername());

        response.sendRedirect(request.getContextPath() + "/annonce-list");
    }
    private void forwardWithErrors(HttpServletRequest request,
                                   HttpServletResponse response,
                                   Map<String, String> errors,
                                   String login) throws ServletException, IOException {
        request.setAttribute("errors", errors);
        request.setAttribute("loginValue", login == null ? "" : login);
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    private Map<String, String> validate(String login, String password) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (login == null || login.isBlank()) {
            errors.put("login", "Le login est obligatoire.");
        }

        if (password == null || password.isBlank()) {
            errors.put("password", "Le mot de passe est obligatoire.");
        }

        return errors;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
