package com.bannergress.backend.comment;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/** Hibernate-managed comment outbox. */
@Entity
@Table(name = "comment_outbox")
class CommentOutboxEvent {
    protected CommentOutboxEvent() {
    }

    @Id
    @Column(name = "id")
    UUID id = UUID.randomUUID();

    @Column(name = "comment_id")
    UUID comment;

    @Column(name = "author_id")
    UUID author;

    @Column(name = "event_type")
    String eventType;

    @Column(name = "environment")
    String environment;

    @Column(name = "created_at")
    Instant createdAt = Instant.now();

    @Column(name = "available_at")
    Instant availableAt = Instant.now();

    @Column(name = "lease_token")
    UUID leaseToken;

    @Column(name = "lease_until")
    Instant leaseUntil;

    @Column(name = "attempts")
    int attempts;

    @Column(name = "state")
    String state = "QUEUED";

    @Column(name = "telegram_message_ids")
    String[] telegramMessageIds = new String[0];

}
