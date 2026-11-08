package com.bannergress.backend.comment;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static com.bannergress.backend.comment.CommentModels.*;
import static org.springframework.http.HttpStatus.FORBIDDEN;

/** Environment names in this route are assertions, never backend/environment selectors. */
@RestController
@RequestMapping("/moderation/comments/{environment}")
@PreAuthorize("hasRole('moderate-comments')")
public class CommentModerationController {
    private final CommentService comments;
    private final CommentOutbox outbox;
    private final CommentSettings settings;

    public CommentModerationController(CommentService comments, CommentOutbox outbox, CommentSettings settings) {
        this.comments = comments;
        this.outbox = outbox;
        this.settings = settings;
    }

    @ModelAttribute
    public void authorize(Authentication authentication, @PathVariable String environment) {
        comments.authorizeBot(authentication, null);
        if (!settings.environment().equals(environment)) {
            throw new ResponseStatusException(FORBIDDEN, "Environment mismatch");
        }
    }

    @GetMapping
    public List<ModerationView> queue(@RequestParam(required = false) UUID author,
        @RequestParam(required = false) State state, @RequestParam(defaultValue = "0") int offset,
        @RequestParam(defaultValue = "30") int limit) {
        return comments.moderationQueue(author, state, offset, limit);
    }

    @GetMapping("/{id}")
    public ModerationView get(@PathVariable UUID id) {
        return comments.moderation(id);
    }

    @PostMapping("/{id}/decisions")
    public ModerationView decide(Authentication authentication, @PathVariable UUID id,
        @Valid @RequestBody Decision request) {
        return comments.decide(authentication, id, request);
    }

    @PostMapping("/authors/{id}/mode")
    public void mode(Authentication authentication, @PathVariable UUID id, @Valid @RequestBody ChangeMode request) {
        comments.changeMode(authentication, id, request);
    }

    @GetMapping("/authors/{id}/audit")
    public List<Audit> audit(@PathVariable UUID id, @RequestParam(defaultValue = "0") int offset,
        @RequestParam(defaultValue = "30") int limit) {
        return comments.history(id, offset, limit);
    }

    @PostMapping("/outbox/claim")
    public List<Delivery> claim(@Valid @RequestBody Claim request) {
        return outbox.claim(request.limit());
    }

    @PostMapping("/outbox/{id}/ack")
    public void acknowledge(@PathVariable UUID id, @Valid @RequestBody Acknowledge request) {
        outbox.acknowledge(id, request);
    }

    @PostMapping("/outbox/{id}/retry")
    public void retry(@PathVariable UUID id, @Valid @RequestBody Retry request) {
        outbox.retry(id, request.leaseToken());
    }

    @PostMapping("/outbox/{id}/requeue")
    public void requeue(@PathVariable UUID id) {
        outbox.requeue(id);
    }

    @GetMapping("/outbox/dead")
    public List<UUID> dead(@RequestParam(defaultValue = "0") int offset,
        @RequestParam(defaultValue = "30") int limit) {
        return outbox.dead(offset, limit);
    }

    @GetMapping("/outbox/stats")
    public QueueStats stats() {
        return outbox.stats();
    }
}
