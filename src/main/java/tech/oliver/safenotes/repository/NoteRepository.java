package tech.oliver.safenotes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.oliver.safenotes.entity.Note;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findAllByOwnerId(Long ownerId);

    boolean existsByIdAndOwnerId(Long noteId, Long ownerId);
}
