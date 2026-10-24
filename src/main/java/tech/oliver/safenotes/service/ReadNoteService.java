package tech.oliver.safenotes.service;

import org.springframework.stereotype.Service;
import tech.oliver.safenotes.controller.dto.ReadNoteResponse;
import tech.oliver.safenotes.repository.NoteRepository;

@Service
public class ReadNoteService {

    private final NoteRepository noteRepository;

    public ReadNoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public ReadNoteResponse readNote(Long noteId) {
        var note = noteRepository.getReferenceById(noteId);

        return ReadNoteResponse.fromEntity(note);
    }
}
