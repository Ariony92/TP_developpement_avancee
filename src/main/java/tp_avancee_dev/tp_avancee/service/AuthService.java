package tp_avancee_dev.tp_avancee.service;

import jakarta.persistence.EntityManager;
import tp_avancee_dev.tp_avancee.db.EntityManagerUtil;
import tp_avancee_dev.tp_avancee.model.User;

import java.util.List;

public class AuthService {

    public User login(String login, String password) {
        EntityManager em = EntityManagerUtil.createEntityManager();
        try {
            List<User> users = em.createQuery(
                            "SELECT u FROM User u WHERE (u.username = :login OR u.email = :login) AND u.password = :password",
                            User.class
                    )
                    .setParameter("login", login)
                    .setParameter("password", password)
                    .setMaxResults(1)
                    .getResultList();

            return users.isEmpty() ? null : users.get(0);
        } finally {
            em.close();
        }
    }
}
