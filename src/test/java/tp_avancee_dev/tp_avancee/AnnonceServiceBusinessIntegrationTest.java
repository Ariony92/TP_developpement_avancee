package tp_avancee_dev.tp_avancee;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import tp_avancee_dev.tp_avancee.db.EntityManagerUtil;
import tp_avancee_dev.tp_avancee.model.*;
import tp_avancee_dev.tp_avancee.service.AnnonceService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

class AnnonceServiceBusinessIntegrationTest {

    private static EntityManagerFactory emf;
    private EntityManager em;
    private MockedStatic<EntityManagerUtil> mockedUtil;
    private AnnonceService service;
    private Long authorId;
    private Long categoryId;

    @BeforeAll
    static void setupFactory() {
        emf = Persistence.createEntityManagerFactory("tp_avancee_test");
    }

    @AfterAll
    static void closeFactory() {
        if (emf != null) {
            emf.close();
        }
    }

    @BeforeEach
    void setUp() {
        em = emf.createEntityManager();
        seedData();

        mockedUtil = mockStatic(EntityManagerUtil.class);
        mockedUtil.when(EntityManagerUtil::createEntityManager).thenAnswer(invocation -> emf.createEntityManager());

        service = new AnnonceService();
    }

    @AfterEach
    void tearDown() {
        if (mockedUtil != null) {
            mockedUtil.close();
        }
        if (em != null && em.isOpen()) {
            em.close();
        }
    }

    @Test
    void fullBusinessFlow_createPublishSearch_shouldWork() {
        Annonce created = service.createAnnonce(
                "Flow complet", "Annonce de test flow", "Paris", "flow@test.com", authorId, categoryId
        );
        assertNotNull(created.getId());
        assertEquals(Status.DRAFT, created.getStatus());

        Annonce published = service.publishAnnonce(created.getId());
        assertEquals(Status.PUBLISHED, published.getStatus());

        List<Annonce> results = service.searchAnnonces("flow", 1, 10);
        assertTrue(results.stream().anyMatch(a -> a.getId().equals(created.getId())));
    }

    @Test
    void getAnnonceById_shouldLoadRelationsWithoutLazyException() {
        Annonce created = service.createAnnonce(
                "Lazy check", "Verifier lazy", "Lille", "lazy@test.com", authorId, categoryId
        );

        Annonce loaded = service.getAnnonceById(created.getId());
        assertNotNull(loaded);

        assertDoesNotThrow(() -> {
            String username = loaded.getAuthor().getUsername();
            String label = loaded.getCategory().getLabel();
            assertNotNull(username);
            assertNotNull(label);
        });
    }

    private void seedData() {
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Annonce").executeUpdate();
        em.createQuery("DELETE FROM Category").executeUpdate();
        em.createQuery("DELETE FROM User").executeUpdate();

        User user = new User();
        user.setUsername("bizuser");
        user.setEmail("bizuser@test.com");
        user.setPassword("secret");
        em.persist(user);

        Category category = new Category();
        category.setLabel("Business");
        em.persist(category);

        em.getTransaction().commit();
        em.clear();

        authorId = user.getId();
        categoryId = category.getId();
    }
}
