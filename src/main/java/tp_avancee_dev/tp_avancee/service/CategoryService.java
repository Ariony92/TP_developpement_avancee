package tp_avancee_dev.tp_avancee.service;

import jakarta.persistence.EntityManager;
import tp_avancee_dev.tp_avancee.db.EntityManagerUtil;
import tp_avancee_dev.tp_avancee.model.Category;
import tp_avancee_dev.tp_avancee.repository.CategoryRepository;

import java.util.List;

public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService() {
        this.categoryRepository = new CategoryRepository();
    }

    public List<Category> listCategories() {
        EntityManager em = EntityManagerUtil.createEntityManager();
        try {
            return categoryRepository.findAll(em);
        } finally {
            em.close();
        }
    }
}
