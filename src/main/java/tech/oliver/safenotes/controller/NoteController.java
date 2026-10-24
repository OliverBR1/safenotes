package tech.oliver.safenotes.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import tech.oliver.safenotes.controller.dto.CreateNoteRequest;
import tech.oliver.safenotes.controller.dto.NoteResponse;
import tech.oliver.safenotes.controller.dto.ReadNoteResponse;
import tech.oliver.safenotes.controller.dto.UpdateNoteRequest;
import tech.oliver.safenotes.service.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/notes")
public class NoteController {

    private final ListNoteService listNoteService;
    private final CreateNoteService createNoteService;
    private final UpdateNoteService updateNoteService;
    private final DeleteNoteService deleteNoteService;
    private final ReadNoteService readNoteService;

    public NoteController(ListNoteService listNoteService,
                          CreateNoteService createNoteService,
                          UpdateNoteService updateNoteService,
                          DeleteNoteService deleteNoteService,
                          ReadNoteService readNoteService) {
        this.listNoteService = listNoteService;
        this.createNoteService = createNoteService;
        this.updateNoteService = updateNoteService;
        this.deleteNoteService = deleteNoteService;
        this.readNoteService = readNoteService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_note:read')")
    public ResponseEntity<List<NoteResponse>> listNotes(@AuthenticationPrincipal Jwt jwt) {

        var notes = listNoteService.listNotes(Long.parseLong(jwt.getSubject()));

        return ResponseEntity.ok(notes);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_note:write')")
    public ResponseEntity<NoteResponse> createNote(@AuthenticationPrincipal Jwt jwt,
                                                   @RequestBody CreateNoteRequest req) {

        var resp = createNoteService.createNote(Long.parseLong(jwt.getSubject()), req.title(), req.content());

        return ResponseEntity.created(URI.create("/notes/" + resp.id())).build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_note:write') and @noteAuthz.hasAccess(#id, #jwt)")
    public ResponseEntity<Void> updateNote(@P("jwt") @AuthenticationPrincipal Jwt jwt,
                                           @P("id") @PathVariable Long id,
                                           @RequestBody UpdateNoteRequest req) {

        updateNoteService.updateNote(id, req);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_note:write') and @noteAuthz.hasAccess(#id, #jwt)")
    public ResponseEntity<Void> deleteNote(@P("jwt") @AuthenticationPrincipal Jwt jwt,
                                           @P("id") @PathVariable Long id) {

        deleteNoteService.deleteNote(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_note:read') and @noteAuthz.hasAccess(#id, #jwt)")
    public ResponseEntity<ReadNoteResponse> readNote(@P("jwt") @AuthenticationPrincipal Jwt jwt,
                                                     @P("id") @PathVariable Long id) {

        var note = readNoteService.readNote(id);

        return ResponseEntity.ok(note);
    }
}