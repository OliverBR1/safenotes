package tech.oliver.safenotes.config;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.oliver.safenotes.repository.NoteRepository;

@Component("noteAuthz")
public class NoteAuthz {

    private final NoteRepository noteRepository;

    public NoteAuthz(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @Transactional(readOnly = true)
    public boolean hasAccess(Long noteId, Jwt jwt) {
        var userId = Long.valueOf(jwt.getSubject());
        return noteRepository.existsByIdAndOwnerId(noteId, userId);
    }
}
