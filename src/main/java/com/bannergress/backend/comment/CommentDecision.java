package com.bannergress.backend.comment;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/** Hibernate-managed comment decision. */
@Entity
@Table(name = "comment_decision")
class CommentDecision {
    protected CommentDecision() {
    }

    @Id
    @Column(name = "id")
    UUID id = UUID.randomUUID();

    @Column(name = "comment_id")
    UUID comment;

    @Column(name = "author_id")
    UUID author;

    @Column(name = "actor")
    String actor;

    @Column(name = "moderator_id")
    Long moderator;

    @Column(name = "request_key")
    UUID requestKey;

    @Column(name = "action")
    String action;

    @Column(name = "expected_version")
    long expectedVersion;

    @Column(name = "reason")
    String reason;

    @Column(name = "previous_state")
    String previousState;

    @Column(name = "next_state")
    String nextState;

    @Column(name = "created_at")
    Instant createdAt = Instant.now();

}
