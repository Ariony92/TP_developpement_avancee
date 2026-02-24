package tp_avancee_dev.tp_avancee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tp_avancee_dev.tp_avancee.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}
