package tp_avancee_dev.tp_avancee.service;

import jakarta.persistence.EntityManager;
import tp_avancee_dev.tp_avancee.api.exceptions.BusinessConflictException;
import tp_avancee_dev.tp_avancee.db.EntityManagerUtil;
import tp_avancee_dev.tp_avancee.model.Annonce;
import tp_avancee_dev.tp_avancee.model.Category;
import tp_avancee_dev.tp_avancee.model.Status;
import tp_avancee_dev.tp_avancee.model.User;
import tp_avancee_dev.tp_avancee.repository.AnnonceRepository;

import java.util.List;

public class AnnonceService {

    private final AnnonceRepository annonceRepository;

    public AnnonceService() {
        this(new AnnonceRepository());
    }

    public AnnonceService(AnnonceRepository annonceRepository) {
        this.annonceRepository = annonceRepository;
    }

    public Annonce getAnnonceById(Long id) {
        EntityManager em = EntityManagerUtil.createEntityManager();
        try {
            return annonceRepository.findByIdWithRelations(em, id);
        } finally {
            em.close();
        }
    }

    public Annonce createAnnonce(String title,
                                 String description,
                                 String adress,
                                 String mail,
                                 Long authorId,
                                 Long categoryId) {
        EntityManager em = EntityManagerUtil.createEntityManager();
        try {
            em.getTransaction().begin();

            User author = em.find(User.class, authorId);
            Category category = em.find(Category.class, categoryId);

            if (author == null) {
                throw new IllegalArgumentException("Auteur introuvable : id=" + authorId);
            }
            if (category == null) {
                throw new IllegalArgumentException("Catégorie introuvable : id=" + categoryId);
            }

            Annonce annonce = new Annonce(title, description, adress, mail);
            annonce.setAuthor(author);
            annonce.setCategory(category);
            annonce.setStatus(Status.DRAFT);

            annonceRepository.create(em, annonce);
            em.getTransaction().commit();
            return annonce;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public Annonce updateAnnonce(Long annonceId,
                                 String title,
                                 String description,
                                 String adress,
                                 String mail,
                                 Long categoryId) {
        EntityManager em = EntityManagerUtil.createEntityManager();
        try {
            em.getTransaction().begin();

            Annonce annonce = annonceRepository.findById(em, annonceId);
            if (annonce == null) {
                throw new IllegalArgumentException("Annonce introuvable : id=" + annonceId);
            }

            annonce.setTitle(title);
            annonce.setDescription(description);
            annonce.setAdress(adress);
            annonce.setMail(mail);

            Category category = em.find(Category.class, categoryId);
            if (category == null) {
                throw new IllegalArgumentException("Catégorie introuvable : id=" + categoryId);
            }
            annonce.setCategory(category);

            Annonce updated = annonceRepository.update(em, annonce);
            em.getTransaction().commit();
            return updated;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public Annonce publishAnnonce(Long annonceId) {
        return changeStatus(annonceId, Status.PUBLISHED);
    }

    public Annonce archiveAnnonce(Long annonceId) {
        return changeStatus(annonceId, Status.ARCHIVED);
    }

    public boolean deleteAnnonce(Long annonceId) {
        EntityManager em = EntityManagerUtil.createEntityManager();
        try {
            em.getTransaction().begin();

            Annonce annonce = annonceRepository.findById(em, annonceId);
            if (annonce == null) {
                em.getTransaction().commit();
                return false;
            }

            annonceRepository.delete(em, annonce);
            em.getTransaction().commit();
            return true;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public List<Annonce> searchAnnonces(String keyword, int page, int size) {
        EntityManager em = EntityManagerUtil.createEntityManager();
        try {
            return annonceRepository.searchByKeywordPaginated(em, keyword, page, size);
        } finally {
            em.close();
        }
    }

    public List<Annonce> listAnnoncesPaginated(int page, int size) {
        EntityManager em = EntityManagerUtil.createEntityManager();
        try {
            return annonceRepository.findAllPaginated(em, page, size);
        } finally {
            em.close();
        }
    }

    public List<Annonce> listByCategoryAndStatus(Long categoryId, Status status, int page, int size) {
        EntityManager em = EntityManagerUtil.createEntityManager();
        try {
            return annonceRepository.findByCategoryAndStatus(em, categoryId, status, page, size);
        } finally {
            em.close();
        }
    }

    public Annonce changeStatusTo(Long annonceId, Status newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Le statut est obligatoire");
        }
        return changeStatus(annonceId, newStatus);
    }

    private Annonce changeStatus(Long annonceId, Status newStatus) {
        EntityManager em = EntityManagerUtil.createEntityManager();
        try {
            em.getTransaction().begin();

            Annonce annonce = annonceRepository.findById(em, annonceId);
            if (annonce == null) {
                throw new IllegalArgumentException("Annonce introuvable : id=" + annonceId);
            }
            if (annonce.getStatus() == newStatus) {
                throw new BusinessConflictException("Le statut est déjà " + newStatus);
            }
            if (annonce.getStatus() == Status.ARCHIVED && newStatus == Status.PUBLISHED) {
                throw new BusinessConflictException("Conflit métier: une annonce ARCHIVED ne peut pas repasser en PUBLISHED");
            }


            annonce.setStatus(newStatus);
            Annonce updated = annonceRepository.update(em, annonce);

            em.getTransaction().commit();
            return updated;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
