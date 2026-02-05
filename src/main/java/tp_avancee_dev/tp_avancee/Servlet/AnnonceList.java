package tp_avancee_dev.tp_avancee.Servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tp_avancee_dev.tp_avancee.dao.AnnonceDAO;
import tp_avancee_dev.tp_avancee.model.Annonce;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "annonceList", value = "/annonce-list")
public class AnnonceList extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            AnnonceDAO dao = new AnnonceDAO();
            List<Annonce> annonces = dao.findAll();

            request.setAttribute("annonces", annonces);
            request.getRequestDispatcher("/AnnonceList.jsp").forward(request, response);

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
