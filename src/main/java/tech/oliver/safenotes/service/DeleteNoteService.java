package tech.oliver.safenotes.service;

import org.springframework.stereotype.Service;
import tech.oliver.safenotes.repository.NoteRepository;

@Service
public class DeleteNoteService {

    private final NoteRepository noteRepository;

    public DeleteNoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public void deleteNote(Long noteId) {
        noteRepository.deleteById(noteId);
    }
}
