package tp_avancee_dev.tp_avancee.Servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tp_avancee_dev.tp_avancee.service.AnnonceService;

import java.io.IOException;

@WebServlet(name = "annonceStatus", value = "/annonce-status")
public class AnnonceStatus extends HttpServlet {

    private final AnnonceService annonceService = new AnnonceService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Long id = parseLong(request.getParameter("id"));
        String action = request.getParameter("action");

        if (id != null && action != null) {
            if ("publish".equalsIgnoreCase(action)) {
                annonceService.publishAnnonce(id);
            } else if ("archive".equalsIgnoreCase(action)) {
                annonceService.archiveAnnonce(id);
            }
        }

        response.sendRedirect("annonce-list");
    }

    private Long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return null;
        }
    }
}
