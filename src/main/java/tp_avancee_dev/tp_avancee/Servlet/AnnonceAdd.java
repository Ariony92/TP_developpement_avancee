package tp_avancee_dev.tp_avancee.Servlet;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tp_avancee_dev.tp_avancee.model.Category;
import tp_avancee_dev.tp_avancee.service.AnnonceService;
import tp_avancee_dev.tp_avancee.service.CategoryService;

import java.io.IOException;
import java.util.List;


@WebServlet(name = "annonceAdd", value = "/annonce-add")
public class AnnonceAdd extends HttpServlet {

    private final AnnonceService annonceService = new AnnonceService();
    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Category> categories = categoryService.listCategories();
        request.setAttribute("categories", categories);
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

        Long categoryId = parseLong(request.getParameter("categoryId"));
        Object userIdObj = request.getSession().getAttribute("userId");
        Long userId = (userIdObj instanceof Long) ? (Long) userIdObj : null;

        if (title == null || title.isBlank()
                || description == null || description.isBlank()
                || adress == null || adress.isBlank()
                || mail == null || mail.isBlank()
                || categoryId == null
                || userId == null) {
            request.setAttribute("error", "Tous les champs sont obligatoires");
            request.setAttribute("categories", categoryService.listCategories());
            request.getRequestDispatcher("/AnnonceAdd.jsp").forward(request, response);
            return;
        }

        try {
            annonceService.createAnnonce(title, description, adress, mail, userId, categoryId);
            response.sendRedirect("annonce-list");
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            request.setAttribute("categories", categoryService.listCategories());
            request.getRequestDispatcher("/AnnonceAdd.jsp").forward(request, response);
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