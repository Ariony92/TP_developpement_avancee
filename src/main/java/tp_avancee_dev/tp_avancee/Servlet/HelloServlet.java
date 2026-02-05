package tp_avancee_dev.tp_avancee.Servlet;

import java.io.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet(name = "helloServlet", value = "/hello-servlet")
public class HelloServlet extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        String mode = request.getParameter("mode");
        PrintWriter sysout = response.getWriter();
        if ("form".equals(mode)) {
            request.getRequestDispatcher("/hello-form.jsp").forward(request, response);
        } else {
            sysout.println("<html><body>");
            sysout.println("<h1>Hello World !</h1>");
            sysout.println("<br><a href='index.jsp'>Retour menu</a>");
            sysout.println("</body></html>");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter sysout = response.getWriter();

        String nom = request.getParameter("nom");

        sysout.println("<html><body>");
        sysout.println("<h1>Hello the World " + nom + " !</h1>");
        sysout.println("<br><a href='index.jsp'>Retour menu</a>");
        sysout.println("</body></html>");
    }


}


