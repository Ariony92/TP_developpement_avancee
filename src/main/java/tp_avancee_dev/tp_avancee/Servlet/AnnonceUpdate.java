package tp_avancee_dev.tp_avancee.Servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tp_avancee_dev.tp_avancee.model.Annonce;
import tp_avancee_dev.tp_avancee.service.AnnonceService;
import tp_avancee_dev.tp_avancee.service.CategoryService;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;


@WebServlet(name = "annonceUpdate", value = "/annonce-update")
public class AnnonceUpdate extends HttpServlet {
    private static final int TITLE_MAX = 64;
    private static final int DESCRIPTION_MAX = 256;
    private static final int ADDRESS_MAX = 64;
    private static final int MAIL_MAX = 64;

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
        request.setAttribute("annonce", annonce);
        request.setAttribute("categories", categoryService.listCategories());
        request.getRequestDispatcher("/AnnonceUpdate.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        Long id = parseLong(request.getParameter("id"));
        String title = normalize(request.getParameter("title"));
        String description = normalize(request.getParameter("description"));
        String adress = normalize(request.getParameter("adress"));
        String mail = normalize(request.getParameter("mail"));
        String categoryIdRaw = request.getParameter("categoryId");
        Long categoryId = parseLong(categoryIdRaw);

        Map<String, String> errors = validate(id, title, description, adress, mail, categoryId);
        if (!errors.isEmpty()) {
            forwardWithErrors(request, response, id, errors, title, description, adress, mail, categoryIdRaw);
            return;
        }

        try {
            annonceService.updateAnnonce(id, title, description, adress, mail, categoryId);
            response.sendRedirect("annonce-list");
        } catch (Exception e) {
            errors.put("global", e.getMessage());
            forwardWithErrors(request, response, id, errors, title, description, adress, mail, categoryIdRaw);
        }
    }
    private void forwardWithErrors(HttpServletRequest request,
                                   HttpServletResponse response,
                                   Long id,
                                   Map<String, String> errors,
                                   String title,
                                   String description,
                                   String adress,
                                   String mail,
                                   String categoryIdRaw) throws ServletException, IOException {
        request.setAttribute("errors", errors);
        request.setAttribute("formId", id);
        request.setAttribute("titleValue", title == null ? "" : title);
        request.setAttribute("descriptionValue", description == null ? "" : description);
        request.setAttribute("adressValue", adress == null ? "" : adress);
        request.setAttribute("mailValue", mail == null ? "" : mail);
        request.setAttribute("categoryIdValue", categoryIdRaw == null ? "" : categoryIdRaw);
        request.setAttribute("categories", categoryService.listCategories());
        request.getRequestDispatcher("/AnnonceUpdate.jsp").forward(request, response);
    }

    private Map<String, String> validate(Long id,
                                         String title,
                                         String description,
                                         String adress,
                                         String mail,
                                         Long categoryId) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (id == null) {
            errors.put("global", "Annonce invalide.");
        }

        if (isBlank(title)) {
            errors.put("title", "Le titre est obligatoire.");
        } else if (title.length() > TITLE_MAX) {
            errors.put("title", "Le titre ne doit pas dépasser 64 caractères.");
        }

        if (isBlank(description)) {
            errors.put("description", "La description est obligatoire.");
        } else if (description.length() > DESCRIPTION_MAX) {
            errors.put("description", "La description ne doit pas dépasser 256 caractères.");
        }

        if (isBlank(adress)) {
            errors.put("adress", "L'adresse est obligatoire.");
        } else if (adress.length() > ADDRESS_MAX) {
            errors.put("adress", "L'adresse ne doit pas dépasser 64 caractères.");
        }

        if (isBlank(mail)) {
            errors.put("mail", "Le mail est obligatoire.");
        } else if (mail.length() > MAIL_MAX) {
            errors.put("mail", "Le mail ne doit pas dépasser 64 caractères.");
        } else if (!mail.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            errors.put("mail", "Le format du mail est invalide.");
        }

        if (categoryId == null) {
            errors.put("categoryId", "La catégorie est obligatoire.");
        }

        return errors;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
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