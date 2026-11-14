package com.bannergress.backend.comment;

import jakarta.persistence.*;
import org.hibernate.envers.Audited;

import java.time.Instant;
import java.util.UUID;

/** Hibernate-managed banner comment. */
@Entity
@Table(name = "banner_comment")
@Audited
class BannerComment {
    protected BannerComment() {
    }

    @Id
    @Column(name = "id")
    UUID id = UUID.randomUUID();

    UUID id() {
        return id;
    }

    @Column(name = "banner_uuid")
    UUID bannerId;

    UUID bannerId() {
        return bannerId;
    }

    @Column(name = "banner_reference")
    UUID bannerReference;

    UUID bannerReference() {
        return bannerReference;
    }

    @Column(name = "banner_title")
    String title;

    String title() {
        return title;
    }

    @Column(name = "banner_slug")
    String slug;

    String slug() {
        return slug;
    }

    @Column(name = "author_id")
    UUID author;

    UUID author() {
        return author;
    }

    @Column(name = "agent_name")
    String agent;

    String agent() {
        return agent;
    }

    @Column(name = "body")
    String body;

    String body() {
        return body;
    }

    @Column(name = "created_at")
    Instant createdAt = Instant.now();

    Instant createdAt() {
        return createdAt;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    CommentModels.State state;

    CommentModels.State state() {
        return state;
    }

    @Column(name = "reviewed")
    boolean reviewed;

    boolean reviewed() {
        return reviewed;
    }

    @Column(name = "strike")
    boolean strike;

    boolean strike() {
        return strike;
    }

    @Column(name = "forgiven")
    boolean forgiven;

    boolean forgiven() {
        return forgiven;
    }

    @Column(name = "reason")
    String reason;

    String reason() {
        return reason;
    }

    @Column(name = "version")
    long version;

    long version() {
        return version;
    }

    @Column(name = "submission_slug")
    String submissionSlug;

    @Column(name = "strike_generation")
    long strikeGeneration;

    @Column(name = "submission_key")
    UUID submissionKey;

    UUID submissionKey() {
        return submissionKey;
    }

}
