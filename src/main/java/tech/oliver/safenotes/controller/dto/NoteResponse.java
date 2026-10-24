package tech.oliver.safenotes.controller.dto;

import tech.oliver.safenotes.entity.Note;

public record NoteResponse(Long id, String title) {

    public static NoteResponse fromEntity(Note note) {
        return new NoteResponse(
                note.getId(),
                note.getTitle()
        );
    }
}
