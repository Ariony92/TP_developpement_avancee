package tp_avancee_dev.tp_avancee.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import tp_avancee_dev.tp_avancee.model.Category;

import java.util.List;

public class CategoryRepository {

    public Category findById(EntityManager em, Long id) {
        return em.find(Category.class, id);
    }

    public List<Category> findAll(EntityManager em) {
        return em.createQuery("SELECT c FROM Category c ORDER BY c.label ASC", Category.class)
                .getResultList();
    }

    public List<Category> findAllPaginated(EntityManager em, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

        TypedQuery<Category> query = em.createQuery(
                "SELECT c FROM Category c ORDER BY c.label ASC",
                Category.class
        );
        query.setFirstResult(offset);
        query.setMaxResults(safeSize);
        return query.getResultList();
    }

    public void create(EntityManager em, Category category) {
        em.persist(category);
    }

    public Category update(EntityManager em, Category category) {
        return em.merge(category);
    }

    public void delete(EntityManager em, Category category) {
        em.remove(category);
    }

    public List<Category> searchByKeyword(EntityManager em, String keyword) {
        String searchTerm = keyword == null ? "" : keyword.trim().toLowerCase();
        return em.createQuery(
                        "SELECT c FROM Category c WHERE LOWER(c.label) LIKE :term ORDER BY c.label ASC",
                        Category.class
                )
                .setParameter("term", "%" + searchTerm + "%")
                .getResultList();
    }
}
