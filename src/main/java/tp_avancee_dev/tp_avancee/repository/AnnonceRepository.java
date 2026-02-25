package tp_avancee_dev.tp_avancee.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import tp_avancee_dev.tp_avancee.model.Annonce;

import java.util.Optional;

public interface AnnonceRepository extends JpaRepository<Annonce, Long>, JpaSpecificationExecutor<Annonce> {

    @EntityGraph(attributePaths = {"author", "category"})
    Optional<Annonce> findWithRelationsById(Long id);
}
