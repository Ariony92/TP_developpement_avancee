package tp_avancee_dev.tp_avancee;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import tp_avancee_dev.tp_avancee.Servlet.AnnonceAdd;
import tp_avancee_dev.tp_avancee.db.EntityManagerUtil;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AnnonceAddServletTest {

    private static EntityManagerFactory emf;
    private MockedStatic<EntityManagerUtil> mockedUtil;

    @BeforeAll
    static void setupFactory() {
        emf = Persistence.createEntityManagerFactory("tp_avancee_test");
    }

    @AfterAll
    static void closeFactory() {
        if (emf != null) emf.close();
    }

    @BeforeEach
    void setup() {
        mockedUtil = mockStatic(EntityManagerUtil.class);
        mockedUtil.when(EntityManagerUtil::createEntityManager)
                .thenAnswer(invocation -> emf.createEntityManager());
    }

    @AfterEach
    void tearDown() {
        mockedUtil.close();
    }

    @Test
    void doPost_shouldForwardWithErrorsAndPreserveValues_whenFormInvalid() throws Exception {
        AnnonceAdd servlet = new AnnonceAdd();

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        RequestDispatcher dispatcher = mock(RequestDispatcher.class);

        when(request.getMethod()).thenReturn("POST");

        when(request.getParameter("title")).thenReturn("Mon titre");
        when(request.getParameter("description")).thenReturn(" ");
        when(request.getParameter("adress")).thenReturn("Paris");
        when(request.getParameter("mail")).thenReturn("bad-mail");
        when(request.getParameter("categoryId")).thenReturn("");

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(1L);

        when(request.getRequestDispatcher("/AnnonceAdd.jsp")).thenReturn(dispatcher);

        servlet.service(request, response);

        verify(request).setCharacterEncoding("UTF-8");
        verify(request).setAttribute(eq("errors"), any());

        verify(request).setAttribute("titleValue", "Mon titre");
        verify(request).setAttribute("descriptionValue", "");
        verify(request).setAttribute("adressValue", "Paris");
        verify(request).setAttribute("mailValue", "bad-mail");

        verify(dispatcher).forward(request, response);

        verify(response, never()).sendRedirect(anyString());
    }
}
