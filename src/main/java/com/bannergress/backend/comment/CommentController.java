package com.bannergress.backend.comment;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

import static com.bannergress.backend.comment.CommentModels.*;

/** Signed-in read policy preserves the existing agent-identity visibility boundary. */
@RestController
@PreAuthorize("isAuthenticated()")
public class CommentController {
    private final CommentService comments;

    public CommentController(CommentService comments) {
        this.comments = comments;
    }

    @GetMapping("/user/comments/capabilities")
    public Capabilities capabilities(Principal principal) {
        return comments.capabilities(principal.getName());
    }

    @GetMapping("/bnrs/{slug}/comments")
    public Page list(@PathVariable String slug, @RequestParam(defaultValue = "0") int offset,
        @RequestParam(defaultValue = "30") int limit) {
        return comments.published(slug, offset, limit);
    }

    @PostMapping("/bnrs/{slug}/comments")
    public View create(Principal principal, @PathVariable String slug, @Valid @RequestBody Create request) {
        return comments.create(principal.getName(), slug, request);
    }

    @GetMapping("/user/comments")
    public Page mine(Principal principal, @RequestParam(defaultValue = "0") int offset,
        @RequestParam(defaultValue = "30") int limit) {
        return comments.mine(principal.getName(), offset, limit);
    }

    @DeleteMapping("/comments/{id}")
    public View withdraw(Principal principal, @PathVariable UUID id, @RequestParam long expectedVersion) {
        return comments.withdraw(principal.getName(), id, expectedVersion);
    }
}
