package tech.oliver.safenotes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.oliver.safenotes.entity.Scope;

import java.util.Optional;

public interface ScopeRepository extends JpaRepository <Scope, Long> {
    Optional<Scope> findByName(String name);
}
