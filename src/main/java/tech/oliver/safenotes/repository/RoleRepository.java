package tech.oliver.safenotes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.oliver.safenotes.entity.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository <Role, Long> {
    Optional<Role> findByName(String name);
}
