package tp_avancee_dev.tp_avancee;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import tp_avancee_dev.tp_avancee.Servlet.LoginServlet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class LoginServletTest {

    @Test
    void doPost_shouldForwardWithFieldErrorsWhenLoginOrPasswordMissing() throws Exception {
        LoginServlet servlet = new LoginServlet();

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getMethod()).thenReturn("POST");

        when(request.getParameter("login")).thenReturn("   ");
        when(request.getParameter("password")).thenReturn("");

        when(request.getRequestDispatcher("/login.jsp")).thenReturn(dispatcher);

        servlet.service(request, response);

        verify(request).setCharacterEncoding("UTF-8");
        verify(request).setAttribute(eq("errors"), any());
        verify(request).setAttribute("loginValue", "");

        verify(dispatcher).forward(request, response);
        verify(response, never()).sendRedirect(anyString());
    }
}
