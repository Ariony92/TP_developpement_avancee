package tp_avancee_dev.tp_avancee.Servlet;

import java.io.IOException;
import java.sql.Timestamp;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tp_avancee_dev.tp_avancee.dao.AnnonceDAO;
import tp_avancee_dev.tp_avancee.model.Annonce;

@WebServlet(name = "annonceAdd", value = "/annonce-add")
public class AnnonceAdd extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher("/AnnonceAdd.jsp").forward(request, response);
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String adress = request.getParameter("adress");
        String mail = request.getParameter("mail");


        if (title == null || title.isBlank()
                || description == null || description.isBlank()
                || adress == null || adress.isBlank()
                || mail == null || mail.isBlank()) {

            request.setAttribute("error", "Tous les champs sont obligatoires");
            request.getRequestDispatcher("/AnnonceAdd.jsp").forward(request, response);
            return;
        }


        Annonce annonce = new Annonce(title, description, adress, mail, new Timestamp(System.currentTimeMillis()));


        try {
            AnnonceDAO dao = new AnnonceDAO();
            boolean ok = dao.create(annonce);

            if (!ok) {
                request.setAttribute("error", "Erreur lors de l'insertion en base");
                request.getRequestDispatcher("/AnnonceAdd.jsp").forward(request, response);
                return;
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }

        response.sendRedirect("index.jsp");
    }
}

