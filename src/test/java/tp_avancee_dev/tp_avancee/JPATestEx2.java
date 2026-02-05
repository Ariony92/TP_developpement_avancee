package tp_avancee_dev.tp_avancee;

import jakarta.persistence.EntityManager;
import tp_avancee_dev.tp_avancee.db.EntityManagerUtil;
import tp_avancee_dev.tp_avancee.model.Annonce;
import tp_avancee_dev.tp_avancee.model.Category;
import tp_avancee_dev.tp_avancee.model.Status;
import tp_avancee_dev.tp_avancee.model.User;

public class JPATestEx2 {
    public static void main(String[] args) {
        EntityManager em = null;

        try {
            em = EntityManagerUtil.createEntityManager();
            em.getTransaction().begin();

            // 1) Category
            Category cat = new Category();
            cat.setLabel("Informatique");
            em.persist(cat);

            // 2) User
            User u = new User();
            u.setUsername("testuser");
            u.setEmail("testuser@mail.com");
            u.setPassword("secret");
            em.persist(u);

            // 3) Annonce (liée à User + Category)
            Annonce a = new Annonce("Titre test", "Description test", "Paris", "contact@mail.com");
            a.setStatus(Status.DRAFT);
            a.setAuthor(u);
            a.setCategory(cat);
            em.persist(a);

            em.getTransaction().commit();

            System.out.println("✅ Ex2 OK : inserts faits. ID annonce = " + a.getId());

        } catch (Exception e) {
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
                System.out.println("⛔ Rollback effectué");
            }
            e.printStackTrace();
        } finally {
            if (em != null && em.isOpen()) em.close();
            EntityManagerUtil.closeEntityManagerFactory();
        }
    }
}
