package org.univ_paris8.iut.montreuil.qdev.tp2025.gr07.jeuquizz.dev_avancee;

import org.univ_paris8.iut.montreuil.qdev.tp2025.gr07.jeuquizz.dev_avancee.dao.AnnonceDAO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.gr07.jeuquizz.dev_avancee.model.Annonce;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/annonce-update")
public class AnnonceUpdate extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            int id = Integer.parseInt(request.getParameter("id"));
            AnnonceDAO dao = new AnnonceDAO();
            Annonce a = dao.find(id);

            if (a == null) {
                response.setContentType("text/html; charset=UTF-8");
                response.getWriter().println("<h1>Annonce introuvable</h1>");
                response.getWriter().println("<a href='annonce-list'>Retour liste</a>");
                return;
            }

            request.setAttribute("annonce", a);
            request.getRequestDispatcher("AnnonceUpdate.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String title = request.getParameter("title");
            String description = request.getParameter("description");
            String adress = request.getParameter("adress");
            String mail = request.getParameter("mail");

            Annonce a = new Annonce();
            a.setId(id);
            a.setTitle(title);
            a.setDescription(description);
            a.setAdress(adress);
            a.setMail(mail);

            AnnonceDAO dao = new AnnonceDAO();
            dao.update(a);

            response.sendRedirect("annonce-list");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}