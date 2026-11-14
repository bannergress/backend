package com.bannergress.backend.comment;

import jakarta.persistence.*;
import org.hibernate.envers.Audited;

import java.util.UUID;

/** Hibernate-managed comment author. */
@Entity
@Table(name = "comment_author")
@Audited
class CommentAuthor {
    protected CommentAuthor() {
    }

    @Id
    @Column(name = "id")
    UUID id = UUID.randomUUID();

    UUID id() {
        return id;
    }

    @Column(name = "subject")
    String subject;

    String subject() {
        return subject;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "mode")
    CommentModels.Mode mode = CommentModels.Mode.POSTMODERATION;

    CommentModels.Mode mode() {
        return mode;
    }

    // Resetting strikes advances one audited author baseline, not every historical comment.
    @Column(name = "strike_generation")
    long strikeGeneration;

    @Column(name = "version")
    long version;

    long version() {
        return version;
    }

}
