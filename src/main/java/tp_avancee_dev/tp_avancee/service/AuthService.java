package tp_avancee_dev.tp_avancee.service;

import jakarta.persistence.EntityManager;
import tp_avancee_dev.tp_avancee.db.EntityManagerUtil;
import tp_avancee_dev.tp_avancee.model.User;

import tp_avancee_dev.tp_avancee.repository.UserRepository;

public class AuthService {

    private final UserRepository userRepository = new UserRepository();

    public User login(String login, String password) {
        EntityManager em = EntityManagerUtil.createEntityManager();
        try {
            return userRepository.findByLoginAndPassword(em, login, password);
        } finally {
            em.close();
        }
    }
}
