package tp_avancee_dev.tp_avancee.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import tp_avancee_dev.tp_avancee.model.Annonce;
import tp_avancee_dev.tp_avancee.model.Status;

import java.util.List;

public class AnnonceRepository {

    public Annonce findById(EntityManager em, Long id) {
        return em.find(Annonce.class, id);
    }


    public Annonce findByIdWithRelations(EntityManager em, Long id) {
        List<Annonce> annonces = em.createQuery(
                        "SELECT a FROM Annonce a " +
                                "LEFT JOIN FETCH a.category " +
                                "LEFT JOIN FETCH a.author " +
                                "WHERE a.id = :id",
                        Annonce.class
                )
                .setParameter("id", id)
                .setMaxResults(1)
                .getResultList();
        return annonces.isEmpty() ? null : annonces.get(0);
    }

    public List<Annonce> findAll(EntityManager em) {
        return em.createQuery("SELECT a FROM Annonce a ORDER BY a.date DESC", Annonce.class)
                .getResultList();
    }

    public List<Annonce> findAllPaginated(EntityManager em, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

        TypedQuery<Annonce> query = em.createQuery(
                "SELECT a FROM Annonce a ORDER BY a.date DESC",
                Annonce.class
        );
        query.setFirstResult(offset);
        query.setMaxResults(safeSize);
        return query.getResultList();
    }

    public void create(EntityManager em, Annonce annonce) {
        em.persist(annonce);
    }

    public Annonce update(EntityManager em, Annonce annonce) {
        return em.merge(annonce);
    }

    public void delete(EntityManager em, Annonce annonce) {
        em.remove(annonce);
    }

    public List<Annonce> searchByKeyword(EntityManager em, String keyword) {
        String searchTerm = keyword == null ? "" : keyword.trim().toLowerCase();

        return em.createQuery(
                        "SELECT a FROM Annonce a " +
                                "WHERE LOWER(a.title) LIKE :term " +
                                "OR LOWER(a.description) LIKE :term " +
                                "OR LOWER(a.adress) LIKE :term " +
                                "ORDER BY a.date DESC",
                        Annonce.class
                )
                .setParameter("term", "%" + searchTerm + "%")
                .getResultList();
    }

    public List<Annonce> searchByKeywordPaginated(EntityManager em, String keyword, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;
        String searchTerm = keyword == null ? "" : keyword.trim().toLowerCase();

        TypedQuery<Annonce> query = em.createQuery(
                "SELECT a FROM Annonce a " +
                        "WHERE LOWER(a.title) LIKE :term " +
                        "OR LOWER(a.description) LIKE :term " +
                        "OR LOWER(a.adress) LIKE :term " +
                        "ORDER BY a.date DESC",
                Annonce.class
        );

        query.setParameter("term", "%" + searchTerm + "%");
        query.setFirstResult(offset);
        query.setMaxResults(safeSize);
        return query.getResultList();
    }

    public List<Annonce> findByCategoryAndStatus(EntityManager em, Long categoryId, Status status, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

        TypedQuery<Annonce> query = em.createQuery(
                "SELECT a FROM Annonce a " +
                        "WHERE a.category.id = :categoryId AND a.status = :status " +
                        "ORDER BY a.date DESC",
                Annonce.class
        );

        query.setParameter("categoryId", categoryId);
        query.setParameter("status", status);
        query.setFirstResult(offset);
        query.setMaxResults(safeSize);

        return query.getResultList();
    }
}
