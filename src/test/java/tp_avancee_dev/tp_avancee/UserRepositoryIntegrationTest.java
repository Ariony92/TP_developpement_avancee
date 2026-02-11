package tp_avancee_dev.tp_avancee;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;
import tp_avancee_dev.tp_avancee.model.User;
import tp_avancee_dev.tp_avancee.repository.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryIntegrationTest {

    private static EntityManagerFactory emf;
    private EntityManager em;
    private UserRepository repository;

    @BeforeAll
    static void setupFactory() {
        emf = Persistence.createEntityManagerFactory("tp_avancee_test");
    }

    @AfterAll
    static void closeFactory() {
        if (emf != null) emf.close();
    }

    @BeforeEach
    void setUp() {
        em = emf.createEntityManager();
        repository = new UserRepository();
        seedData();
    }

    @AfterEach
    void tearDown() {
        if (em != null && em.isOpen()) em.close();
    }

    @Test
    void crud_shouldCreateUpdateDeleteUser() {
        em.getTransaction().begin();
        User user = new User();
        user.setUsername("charlie");
        user.setEmail("charlie@test.com");
        user.setPassword("pwd");
        repository.create(em, user);
        em.getTransaction().commit();

        assertNotNull(user.getId());
        em.clear();

        em.getTransaction().begin();
        user.setUsername("charlie-updated");
        User merged = repository.update(em, user);
        em.getTransaction().commit();

        em.clear();
        User found = repository.findById(em, merged.getId());
        assertEquals("charlie-updated", found.getUsername());

        em.getTransaction().begin();
        repository.delete(em, found);
        em.getTransaction().commit();

        em.clear();
        assertNull(repository.findById(em, found.getId()));
    }

    @Test
    void searchAndPagination_shouldReturnExpectedUsers() {
        List<User> search = repository.searchByKeyword(em, "ali");
        assertFalse(search.isEmpty());
        assertTrue(search.stream().allMatch(u ->
                u.getUsername().toLowerCase().contains("ali")
                        || u.getEmail().toLowerCase().contains("ali")
        ));

        List<User> page1 = repository.findAllPaginated(em, 1, 2);
        List<User> page2 = repository.findAllPaginated(em, 2, 2);

        assertEquals(2, page1.size());
        assertEquals(1, page2.size());

        Set<Long> idsPage1 = new HashSet<>();
        for (User u : page1) idsPage1.add(u.getId());

        assertTrue(page2.stream().noneMatch(u -> idsPage1.contains(u.getId())));
    }

    private void seedData() {
        em.getTransaction().begin();

        em.createQuery("DELETE FROM Annonce").executeUpdate();
        em.createQuery("DELETE FROM Category").executeUpdate();
        em.createQuery("DELETE FROM User").executeUpdate();

        em.persist(buildUser("alice", "alice@test.com"));
        em.persist(buildUser("bob", "bob@test.com"));
        em.persist(buildUser("alicia", "alicia@test.com"));

        em.getTransaction().commit();
        em.clear();
    }

    private User buildUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("secret");
        return user;
    }
}
