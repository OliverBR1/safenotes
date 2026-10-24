package tech.oliver.safenotes.service;

import org.springframework.stereotype.Service;
import tech.oliver.safenotes.controller.dto.NoteResponse;
import tech.oliver.safenotes.repository.NoteRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListNoteService {

    private final NoteRepository noteRepository;

    public ListNoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public List<NoteResponse> listNotes(Long userId) {
        return noteRepository.findAllByOwnerId(userId)
                .stream()
                .map(NoteResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
