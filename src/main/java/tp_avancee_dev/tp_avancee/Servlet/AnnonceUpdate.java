package tp_avancee_dev.tp_avancee.Servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tp_avancee_dev.tp_avancee.dao.AnnonceDAO;
import tp_avancee_dev.tp_avancee.model.Annonce;

import java.io.IOException;

@WebServlet(name = "annonceUpdate", value = "/annonce-update")
public class AnnonceUpdate extends HttpServlet {

    private int getId(HttpServletRequest request) {
        try {
            return Integer.parseInt(request.getParameter("id"));
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int id = getId(request);
        if (id <= 0) {
            response.sendRedirect("annonce-list");
            return;
        }

        try {
            AnnonceDAO dao = new AnnonceDAO();
            Annonce annonce = dao.find(id);

            if (annonce == null) {
                response.sendRedirect("annonce-list");
                return;
            }

            request.setAttribute("annonce", annonce);
            request.getRequestDispatcher("/AnnonceUpdate.jsp").forward(request, response);

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        int id = getId(request);
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String adress = request.getParameter("adress");
        String mail = request.getParameter("mail");

        if (id <= 0
                || title == null || title.isBlank()
                || description == null || description.isBlank()
                || adress == null || adress.isBlank()
                || mail == null || mail.isBlank()) {

            request.setAttribute("error", "Tous les champs sont obligatoires");

            try {
                AnnonceDAO dao = new AnnonceDAO();
                request.setAttribute("annonce", dao.find(id));
            } catch (Exception ignored) {}

            request.getRequestDispatcher("/AnnonceUpdate.jsp").forward(request, response);
            return;
        }

        try {
            Annonce annonce = new Annonce();
            annonce.setId(id);
            annonce.setTitle(title);
            annonce.setDescription(description);
            annonce.setAdress(adress);
            annonce.setMail(mail);

            new AnnonceDAO().update(annonce);

            response.sendRedirect("annonce-list");

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
