package org.univ_paris8.iut.montreuil.qdev.tp2025.gr07.jeuquizz.dev_avancee;

import org.univ_paris8.iut.montreuil.qdev.tp2025.gr07.jeuquizz.dev_avancee.dao.AnnonceDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/annonce-list")
public class AnnonceList extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            AnnonceDAO dao = new AnnonceDAO();
            request.setAttribute("annonces", dao.findAll());
            request.getRequestDispatcher("AnnonceList.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
