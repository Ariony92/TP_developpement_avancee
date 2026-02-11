package tp_avancee_dev.tp_avancee;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;
import tp_avancee_dev.tp_avancee.model.*;
import tp_avancee_dev.tp_avancee.repository.AnnonceRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AnnonceRepositoryIntegrationTest {

    private static EntityManagerFactory emf;
    private EntityManager em;
    private AnnonceRepository repository;

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
        repository = new AnnonceRepository();
        seedData();
    }

    @AfterEach
    void tearDown() {
        if (em != null && em.isOpen()) em.close();
    }

    @Test
    void createAndFindById_shouldPersistAnnonce() {
        em.getTransaction().begin();

        User user = em.createQuery("SELECT u FROM User u", User.class).setMaxResults(1).getSingleResult();
        Category category = em.createQuery("SELECT c FROM Category c", Category.class).setMaxResults(1).getSingleResult();

        Annonce annonce = new Annonce("Nouveau", "Description", "Paris", "nouveau@test.com");
        annonce.setAuthor(user);
        annonce.setCategory(category);
        annonce.setStatus(Status.DRAFT);

        repository.create(em, annonce);
        em.getTransaction().commit();

        assertNotNull(annonce.getId());
        em.clear();

        Annonce found = repository.findById(em, annonce.getId());
        assertNotNull(found);
        assertEquals("Nouveau", found.getTitle());
    }

    @Test
    void update_shouldModifyAnnonce() {
        Annonce annonce = em.createQuery("SELECT a FROM Annonce a WHERE a.title = :title", Annonce.class)
                .setParameter("title", "Velo ville")
                .setMaxResults(1)
                .getSingleResult();

        em.getTransaction().begin();
        annonce.setTitle("Velo ville (modifié)");
        annonce.setMail("modif@test.com");
        repository.update(em, annonce);
        em.getTransaction().commit();

        em.clear();

        Annonce reloaded = repository.findById(em, annonce.getId());
        assertNotNull(reloaded);
        assertEquals("Velo ville (modifié)", reloaded.getTitle());
        assertEquals("modif@test.com", reloaded.getMail());
    }

    @Test
    void delete_shouldRemoveAnnonce() {
        Annonce annonce = em.createQuery("SELECT a FROM Annonce a WHERE a.title = :title", Annonce.class)
                .setParameter("title", "Canape")
                .setMaxResults(1)
                .getSingleResult();

        Long id = annonce.getId();
        assertNotNull(id);

        em.getTransaction().begin();
        repository.delete(em, annonce);
        em.getTransaction().commit();

        em.clear();

        assertNull(repository.findById(em, id));
    }

    @Test
    void searchAndPagination_shouldReturnExpectedResults() {
        List<Annonce> search = repository.searchByKeywordPaginated(em, "velo", 1, 10);
        assertFalse(search.isEmpty());
        assertTrue(search.stream().allMatch(a ->
                a.getTitle().toLowerCase().contains("velo")
                        || a.getDescription().toLowerCase().contains("velo")
                        || a.getAdress().toLowerCase().contains("velo")
        ));

        List<Annonce> page1 = repository.findAllPaginated(em, 1, 2);
        List<Annonce> page2 = repository.findAllPaginated(em, 2, 2);

        assertEquals(2, page1.size());
        assertEquals(2, page2.size());

        Set<Long> idsPage1 = new HashSet<>();
        for (Annonce a : page1) idsPage1.add(a.getId());

        assertTrue(page2.stream().noneMatch(a -> idsPage1.contains(a.getId())));
    }

    @Test
    void findByCategoryAndStatus_shouldFilterCorrectly() {
        Category category = em.createQuery("SELECT c FROM Category c WHERE c.label = :label", Category.class)
                .setParameter("label", "Sport")
                .getSingleResult();

        List<Annonce> filtered = repository.findByCategoryAndStatus(em, category.getId(), Status.PUBLISHED, 1, 10);

        assertFalse(filtered.isEmpty());
        assertTrue(filtered.stream().allMatch(a ->
                a.getCategory().getId().equals(category.getId()) && a.getStatus() == Status.PUBLISHED
        ));
    }

    private void seedData() {
        em.getTransaction().begin();

        em.createQuery("DELETE FROM Annonce").executeUpdate();
        em.createQuery("DELETE FROM Category").executeUpdate();
        em.createQuery("DELETE FROM User").executeUpdate();

        User user = new User();
        user.setUsername("alice");
        user.setEmail("alice@test.com");
        user.setPassword("secret");
        em.persist(user);

        Category sport = new Category();
        sport.setLabel("Sport");
        em.persist(sport);

        Category maison = new Category();
        maison.setLabel("Maison");
        em.persist(maison);

        em.persist(buildAnnonce("Velo route", "Super velo carbone", "Lille", "velo1@test.com", user, sport, Status.PUBLISHED));
        em.persist(buildAnnonce("Velo ville", "Velo pratique", "Paris", "velo2@test.com", user, sport, Status.DRAFT));
        em.persist(buildAnnonce("Canape", "Canape 3 places", "Lyon", "canape@test.com", user, maison, Status.ARCHIVED));
        em.persist(buildAnnonce("Tapis velo", "Accessoire fitness", "Nantes", "fitness@test.com", user, sport, Status.PUBLISHED));

        em.getTransaction().commit();
        em.clear();
    }

    private Annonce buildAnnonce(String title,
                                 String description,
                                 String address,
                                 String mail,
                                 User author,
                                 Category category,
                                 Status status) {
        Annonce annonce = new Annonce(title, description, address, mail);
        annonce.setAuthor(author);
        annonce.setCategory(category);
        annonce.setStatus(status);
        return annonce;
    }
}
