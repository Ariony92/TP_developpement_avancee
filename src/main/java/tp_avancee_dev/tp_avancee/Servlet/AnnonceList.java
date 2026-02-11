package tp_avancee_dev.tp_avancee.Servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tp_avancee_dev.tp_avancee.model.Annonce;
import tp_avancee_dev.tp_avancee.model.Category;
import tp_avancee_dev.tp_avancee.model.Status;
import tp_avancee_dev.tp_avancee.service.AnnonceService;
import tp_avancee_dev.tp_avancee.service.CategoryService;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "annonceList", value = "/annonce-list")
public class AnnonceList extends HttpServlet {

    private final AnnonceService annonceService = new AnnonceService();
    private final CategoryService categoryService = new CategoryService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int page = parseInt(request.getParameter("page"), 1);
        int size = parseInt(request.getParameter("size"), 5);
        String keyword = trimToNull(request.getParameter("q"));

        Long categoryId = parseLongOrNull(request.getParameter("categoryId"));
        Status status = parseStatusOrNull(request.getParameter("status"));

        List<Annonce> annonces;
        if (categoryId != null && status != null) {
            annonces = annonceService.listByCategoryAndStatus(categoryId, status, page, size);
        } else if (keyword != null) {
            annonces = annonceService.searchAnnonces(keyword, page, size);
        } else {
            annonces = annonceService.listAnnoncesPaginated(page, size);
        }

        List<Category> categories = categoryService.listCategories();

        request.setAttribute("annonces", annonces);
        request.setAttribute("categories", categories);
        request.setAttribute("statuses", Status.values());
        request.setAttribute("page", page);
        request.setAttribute("size", size);
        request.setAttribute("q", keyword == null ? "" : keyword);
        request.setAttribute("categoryId", categoryId);
        request.setAttribute("selectedStatus", status == null ? "" : status.name());
        request.setAttribute("hasNext", annonces.size() == size);

        request.getRequestDispatcher("/AnnonceList.jsp").forward(request, response);
    }

    private int parseInt(String value, int defaultValue) {
        try { return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private Long parseLongOrNull(String value) {
        try {
            if (value == null || value.isBlank()) return null;
            return Long.parseLong(value);
        } catch (Exception e) {
            return null;
        }
    }
    private Status parseStatusOrNull(String value) {
        try {
            if (value == null || value.isBlank()) return null;
            return Status.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String v = value.trim();
        return v.isEmpty() ? null : v;
    }
}
