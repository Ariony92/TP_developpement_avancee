package tp_avancee_dev.tp_avancee;

import jakarta.persistence.EntityManager;
import tp_avancee_dev.tp_avancee.db.EntityManagerUtil;

public class main {
    public static void main(String[] args) {
        EntityManager em = EntityManagerUtil.createEntityManager();
        em.getTransaction().begin();

        // persist User, Category, Annonce

        em.getTransaction().commit();
        em.close();
        EntityManagerUtil.closeEntityManagerFactory();
        System.out.println("OK EX2");
    }
}
