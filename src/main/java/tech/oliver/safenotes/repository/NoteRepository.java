package tech.oliver.safenotes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.oliver.safenotes.entity.Note;

public interface NoteRepository extends JpaRepository<Note, Long> {
}
