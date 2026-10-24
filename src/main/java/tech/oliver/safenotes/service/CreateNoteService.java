package tech.oliver.safenotes.service;

import org.springframework.stereotype.Service;
import tech.oliver.safenotes.controller.dto.NoteResponse;
import tech.oliver.safenotes.entity.Note;
import tech.oliver.safenotes.repository.NoteRepository;
import tech.oliver.safenotes.repository.UserRepository;

@Service
public class CreateNoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public CreateNoteService(NoteRepository noteRepository,
                             UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    public NoteResponse createNote(Long userId, String title, String content) {

        var user = userRepository.getReferenceById(userId);

        var note = new Note(title, content, user);

        noteRepository.save(note);

        return NoteResponse.fromEntity(note);
    }
}
