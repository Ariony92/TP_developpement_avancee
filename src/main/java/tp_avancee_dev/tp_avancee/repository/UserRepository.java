package tp_avancee_dev.tp_avancee.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import tp_avancee_dev.tp_avancee.model.User;

import java.util.List;

public class UserRepository {

    public User findByLoginAndPassword(EntityManager em, String login, String password) {
        List<User> users = em.createQuery(
                        "SELECT u FROM User u WHERE (u.username = :login OR u.email = :login) AND u.password = :password",
                        User.class
                )
                .setParameter("login", login)
                .setParameter("password", password)
                .setMaxResults(1)
                .getResultList();

        return users.isEmpty() ? null : users.get(0);
    }

    public User findById(EntityManager em, Long id) {
        return em.find(User.class, id);
    }

    public List<User> findAll(EntityManager em) {
        return em.createQuery("SELECT u FROM User u ORDER BY u.createdAt DESC", User.class)
                .getResultList();
    }

    public List<User> findAllPaginated(EntityManager em, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

        TypedQuery<User> query = em.createQuery(
                "SELECT u FROM User u ORDER BY u.createdAt DESC",
                User.class
        );
        query.setFirstResult(offset);
        query.setMaxResults(safeSize);
        return query.getResultList();
    }

    public void create(EntityManager em, User user) {
        em.persist(user);
    }

    public User update(EntityManager em, User user) {
        return em.merge(user);
    }

    public void delete(EntityManager em, User user) {
        em.remove(user);
    }

    public List<User> searchByKeyword(EntityManager em, String keyword) {
        String searchTerm = keyword == null ? "" : keyword.trim().toLowerCase();
        return em.createQuery(
                        "SELECT u FROM User u " +
                                "WHERE LOWER(u.username) LIKE :term OR LOWER(u.email) LIKE :term " +
                                "ORDER BY u.createdAt DESC",
                        User.class
                )
                .setParameter("term", "%" + searchTerm + "%")
                .getResultList();
    }
}
