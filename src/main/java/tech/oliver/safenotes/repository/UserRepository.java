package tech.oliver.safenotes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.oliver.safenotes.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    User save( User user);
}
