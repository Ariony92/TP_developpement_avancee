package tp_avancee_dev.tp_avancee.Servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tp_avancee_dev.tp_avancee.dao.AnnonceDAO;

import java.io.IOException;

@WebServlet(name = "annonceDelete", value = "/annonce-delete")
public class AnnonceDelete extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isBlank()) {
            response.sendRedirect("annonce-list");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);

            AnnonceDAO dao = new AnnonceDAO();
            dao.delete(id);

            response.sendRedirect("annonce-list");

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
