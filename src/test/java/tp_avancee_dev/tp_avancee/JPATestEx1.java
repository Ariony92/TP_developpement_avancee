package tp_avancee_dev.tp_avancee;

import jakarta.persistence.EntityManager;
import tp_avancee_dev.tp_avancee.db.EntityManagerUtil;

public class JPATestEx1 {
    public static void main(String[] args) {
        EntityManager em = null;
        try {
            em = EntityManagerUtil.createEntityManager();
            System.out.println("✅ EntityManager créé : " + em);

            em.getTransaction().begin();
            System.out.println("✅ Transaction ouverte");

            em.getTransaction().commit();
            System.out.println("✅ Transaction commit");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (em != null && em.isOpen()) em.close();
            EntityManagerUtil.closeEntityManagerFactory();
            System.out.println("✅ Fermeture OK");
        }
    }
}
