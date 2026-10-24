package tech.oliver.safenotes.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.oliver.safenotes.controller.dto.UpdateNoteRequest;
import tech.oliver.safenotes.repository.NoteRepository;

@Service
public class UpdateNoteService {

    private final NoteRepository noteRepository;

    public UpdateNoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @Transactional
    public void updateNote(Long noteId, UpdateNoteRequest req) {

        var note = noteRepository.getReferenceById(noteId);

        note.setTitle(req.title());
        note.setContent(req.content());

        noteRepository.save(note);
    }
}
