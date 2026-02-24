package tp_avancee_dev.tp_avancee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tp_avancee_dev.tp_avancee.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
