package org.univ_paris8.iut.montreuil.qdev.tp2025.gr07.jeuquizz.dev_avancee;

import org.univ_paris8.iut.montreuil.qdev.tp2025.gr07.jeuquizz.dev_avancee.dao.AnnonceDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/annonce-delete")
public class AnnonceDelete extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            int id = Integer.parseInt(request.getParameter("id"));
            AnnonceDAO dao = new AnnonceDAO();
            dao.delete(id);
            response.sendRedirect("annonce-list");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
