package tp_avancee_dev.tp_avancee.Servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tp_avancee_dev.tp_avancee.model.Annonce;
import tp_avancee_dev.tp_avancee.service.AnnonceService;

import java.io.IOException;

@WebServlet(name = "annonceDetail", value = "/annonce-detail")
public class AnnonceDetail extends HttpServlet {

    private final AnnonceService annonceService = new AnnonceService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Long id = parseLong(request.getParameter("id"));
        if (id == null) {
            response.sendRedirect("annonce-list");
            return;
        }

        Annonce annonce = annonceService.getAnnonceById(id);
        if (annonce == null) {
            response.sendRedirect("annonce-list");
            return;
        }

        request.setAttribute("annonce", annonce);
        request.getRequestDispatcher("/AnnonceDetail.jsp").forward(request, response);
    }

    private Long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return null;
        }
    }
}
