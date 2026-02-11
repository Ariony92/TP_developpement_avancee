package tp_avancee_dev.tp_avancee.Servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tp_avancee_dev.tp_avancee.model.Annonce;
import tp_avancee_dev.tp_avancee.model.Category;
import tp_avancee_dev.tp_avancee.service.AnnonceService;
import tp_avancee_dev.tp_avancee.service.CategoryService;

import java.io.IOException;
import java.util.List;


@WebServlet(name = "annonceUpdate", value = "/annonce-update")
public class AnnonceUpdate extends HttpServlet {

    private final AnnonceService annonceService = new AnnonceService();
    private final CategoryService categoryService = new CategoryService();

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
        List<Category> categories = categoryService.listCategories();
        request.setAttribute("annonce", annonce);
        request.setAttribute("categories", categories);
        request.getRequestDispatcher("/AnnonceUpdate.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        Long id = parseLong(request.getParameter("id"));
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String adress = request.getParameter("adress");
        String mail = request.getParameter("mail");
        Long categoryId = parseLong(request.getParameter("categoryId"));

        if (id == null
                || title == null || title.isBlank()
                || description == null || description.isBlank()
                || adress == null || adress.isBlank()
                || mail == null || mail.isBlank()
                || categoryId == null) {

            request.setAttribute("error", "Tous les champs sont obligatoires");


            request.setAttribute("annonce", annonceService.getAnnonceById(id));
            request.setAttribute("categories", categoryService.listCategories());
            request.getRequestDispatcher("/AnnonceUpdate.jsp").forward(request, response);
            return;
        }

        try {
            annonceService.updateAnnonce(id, title, description, adress, mail, categoryId);
            response.sendRedirect("annonce-list");
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("annonce", annonceService.getAnnonceById(id));
            request.setAttribute("categories", categoryService.listCategories());
            request.getRequestDispatcher("/AnnonceUpdate.jsp").forward(request, response);
        }
    }

    private Long parseLong(String value) {
        try {
            if (value == null || value.isBlank()) return null;
            return Long.parseLong(value);
        } catch (Exception e) {
            return null;
        }
    }

}