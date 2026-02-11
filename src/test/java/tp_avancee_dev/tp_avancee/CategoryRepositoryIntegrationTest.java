package tp_avancee_dev.tp_avancee;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;
import tp_avancee_dev.tp_avancee.model.Category;
import tp_avancee_dev.tp_avancee.repository.CategoryRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CategoryRepositoryIntegrationTest {

    private static EntityManagerFactory emf;
    private EntityManager em;
    private CategoryRepository repository;

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
        repository = new CategoryRepository();
        seedData();
    }

    @AfterEach
    void tearDown() {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }

    @Test
    void crud_shouldCreateUpdateDeleteCategory() {
        em.getTransaction().begin();
        Category category = new Category();
        category.setLabel("Jardinage");
        repository.create(em, category);
        em.getTransaction().commit();

        assertNotNull(category.getId());

        em.getTransaction().begin();
        category.setLabel("Jardin");
        Category merged = repository.update(em, category);
        em.getTransaction().commit();

        Category found = repository.findById(em, merged.getId());
        assertEquals("Jardin", found.getLabel());

        em.getTransaction().begin();
        repository.delete(em, found);
        em.getTransaction().commit();

        assertNull(repository.findById(em, found.getId()));
    }

    @Test
    void searchAndPagination_shouldReturnExpectedCategories() {
        List<Category> search = repository.searchByKeyword(em, "sport");
        assertFalse(search.isEmpty());
        assertTrue(search.stream().allMatch(c -> c.getLabel().toLowerCase().contains("sport")));

        List<Category> page1 = repository.findAllPaginated(em, 1, 2);
        List<Category> page2 = repository.findAllPaginated(em, 2, 2);

        assertEquals(2, page1.size());
        assertEquals(1, page2.size());
        assertNotEquals(page1.get(0).getId(), page2.get(0).getId());
    }

    private void seedData() {
        em.getTransaction().begin();
        em.createQuery("DELETE FROM Annonce").executeUpdate();
        em.createQuery("DELETE FROM Category").executeUpdate();

        em.persist(buildCategory("Sport"));
        em.persist(buildCategory("Maison"));
        em.persist(buildCategory("Sport automobile"));

        em.getTransaction().commit();
        em.clear();
    }

    private Category buildCategory(String label) {
        Category category = new Category();
        category.setLabel(label);
        return category;
    }
}
