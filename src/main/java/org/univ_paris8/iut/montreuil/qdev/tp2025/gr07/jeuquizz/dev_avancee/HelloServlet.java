package org.univ_paris8.iut.montreuil.qdev.tp2025.gr07.jeuquizz.dev_avancee;

import java.io.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebServlet(name = "helloServlet", value = "/hello-servlet")
public class HelloServlet extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String mode = request.getParameter("mode");

        out.println("<html><body>");

        if ("hello".equals(mode)) {
            out.println("<h1>Hello World!</h1>");
        } else if ("form".equals(mode)) {
            out.println("<h1>Exercice 3</h1>");
            out.println("<form method='post' action='hello-servlet'>");
            out.println("Ton nom : <input type='text' name='nom' required>");
            out.println("<button type='submit'>Envoyer</button>");
            out.println("</form>");
        } else {
            out.println("<h1>Hello World!</h1>");
        }
    }


        public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String nom = request.getParameter("nom");

        out.println("<html><body>");
        out.println("<h1>Hello World " + nom + " !</h1>");
        out.println("<br><a href='index.jsp'>Retour menu</a>");
        out.println("</body></html>");
    }
}
