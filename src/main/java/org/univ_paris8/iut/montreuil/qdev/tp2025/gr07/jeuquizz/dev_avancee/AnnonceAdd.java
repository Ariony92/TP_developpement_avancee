package org.univ_paris8.iut.montreuil.qdev.tp2025.gr07.jeuquizz.dev_avancee;

import org.univ_paris8.iut.montreuil.qdev.tp2025.gr07.jeuquizz.dev_avancee.db.ConnectionDB;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

@WebServlet("/annonce-add")
public class AnnonceAdd extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("AnnonceAdd.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String adress = request.getParameter("adress");
        String mail = request.getParameter("mail");

        if (isEmpty(title) || isEmpty(description) || isEmpty(adress) || isEmpty(mail)) {
            response.setContentType("text/html; charset=UTF-8");
            response.getWriter().println("<h1>Erreur : tous les champs sont obligatoires</h1>");
            response.getWriter().println("<a href='annonce-add'>Retour</a>");
            return;
        }

        try {
            Connection c = ConnectionDB.getInstance();
            PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO annonce(title, description, adress, mail, date) VALUES (?,?,?,?,now())"
            );
            ps.setString(1, title);
            ps.setString(2, description);
            ps.setString(3, adress);
            ps.setString(4, mail);
            ps.executeUpdate();

            response.sendRedirect("annonce-list");

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}