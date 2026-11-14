package com.bannergress.backend.comment;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/** Hibernate persistence; author row locks serialize every domain mutation, including API version increments. */
@Repository
public class CommentStore {
    @PersistenceContext
    private EntityManager entities;
    private final CommentAuthorProvisioner provisioner;

    public CommentStore(CommentAuthorProvisioner provisioner) {
        this.provisioner = provisioner;
    }

    EntityManager entities() {
        return entities;
    }

    CommentAuthor lockAuthor(String subject) {
        var authors = entities.createQuery("from CommentAuthor where subject = :subject", CommentAuthor.class)
            .setParameter("subject", subject).getResultList();
        if (authors.isEmpty()) {
            try {
                provisioner.ensure(subject);
            } catch (RuntimeException exception) {
                if (!constraint(exception, "comment_author_subject_key")) {
                    throw exception;
                }
                // The winning concurrent transaction committed the same subject. Read it in this transaction.
            }
            authors = entities.createQuery("from CommentAuthor where subject = :subject", CommentAuthor.class)
                .setParameter("subject", subject).getResultList();
        }
        var author = one(authors);
        entities.refresh(author, LockModeType.PESSIMISTIC_WRITE);
        return author;
    }

    CommentAuthor lockAuthor(UUID id) {
        var author = author(id);
        entities.refresh(author, LockModeType.PESSIMISTIC_WRITE);
        return author;
    }

    CommentAuthor author(UUID id) {
        return required(entities.find(CommentAuthor.class, id));
    }

    BannerComment comment(UUID id) {
        return required(entities.find(BannerComment.class, id));
    }

    BannerComment refreshComment(UUID id) {
        var comment = comment(id);
        entities.refresh(comment);
        return comment;
    }

    Instant now() {
        // Database wall time keeps lease fencing consistent across backend instances.
        return entities.createQuery("select cast(function('clock_timestamp') as Instant)", Instant.class).getSingleResult();
    }

    int strikes(UUID author) {
        return entities.createQuery("select count(c) from BannerComment c where c.author = :author and c.strike = true and c.forgiven = false and c.strikeGeneration = (select a.strikeGeneration from CommentAuthor a where a.id = :author)",
            Long.class).setParameter("author", author).getSingleResult().intValue();
    }

    void event(UUID comment, UUID author, String type, String environment) {
        var event = new CommentOutboxEvent();
        event.createdAt = now();
        event.availableAt = event.createdAt;
        event.comment = comment;
        event.author = author;
        event.eventType = type;
        event.environment = environment;
        entities.persist(event);
    }

    static boolean constraint(Throwable error, String name) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation && name.equals(violation.getConstraintName())) {
                return true;
            }
        }
        return false;
    }

    static <T> T required(T value) {
        if (value == null) {
            throw new ResponseStatusException(NOT_FOUND);
        }
        return value;
    }

    static <T> T one(List<T> values) {
        return required(values.isEmpty() ? null : values.getFirst());
    }
}
