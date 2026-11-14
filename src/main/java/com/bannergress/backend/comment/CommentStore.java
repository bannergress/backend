package com.bannergress.backend.comment;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/** PostgreSQL store. Every author mutation must first take the same transaction-scoped row lock. */
@Repository
public class CommentStore {
    final JdbcTemplate jdbc;

    public CommentStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    record Author(UUID id, String subject, CommentModels.Mode mode, long version) {
    }

    record Comment(UUID id, UUID bannerId, UUID bannerReference, String title, String slug,
        UUID author, String agent, String body, Instant createdAt, CommentModels.State state,
        boolean reviewed, boolean strike, boolean forgiven, String reason, long version) {
    }

    Author lockAuthor(String subject) {
        jdbc.update("INSERT INTO comment_author(id, subject) VALUES (?, ?) ON CONFLICT(subject) DO NOTHING",
            UUID.randomUUID(), subject);
        return jdbc.queryForObject("SELECT * FROM comment_author WHERE subject = ? FOR UPDATE", this::mapAuthor, subject);
    }

    Author lockAuthor(UUID id) {
        return one(jdbc.query("SELECT * FROM comment_author WHERE id = ? FOR UPDATE", this::mapAuthor, id));
    }

    Author author(UUID id) {
        return one(jdbc.query("SELECT * FROM comment_author WHERE id = ?", this::mapAuthor, id));
    }

    Comment comment(UUID id) {
        return one(jdbc.query("SELECT * FROM banner_comment WHERE id = ?", this::mapComment, id));
    }

    int strikes(UUID author) {
        return jdbc.queryForObject("SELECT count(*) FROM banner_comment WHERE author_id = ? AND strike AND NOT forgiven",
            Integer.class, author);
    }

    void event(UUID comment, UUID author, String type, String environment) {
        jdbc.update("INSERT INTO comment_outbox(id, comment_id, author_id, event_type, environment) VALUES (?, ?, ?, ?, ?)",
            UUID.randomUUID(), comment, author, type, environment);
    }

    private Author mapAuthor(ResultSet r, int row) throws SQLException {
        return new Author(r.getObject("id", UUID.class), r.getString("subject"),
            CommentModels.Mode.valueOf(r.getString("mode")), r.getLong("version"));
    }

    Comment mapComment(ResultSet r, int row) throws SQLException {
        return new Comment(r.getObject("id", UUID.class), r.getObject("banner_uuid", UUID.class),
            r.getObject("banner_reference", UUID.class), r.getString("banner_title"), r.getString("banner_slug"),
            r.getObject("author_id", UUID.class), r.getString("agent_name"), r.getString("body"),
            r.getTimestamp("created_at").toInstant(), CommentModels.State.valueOf(r.getString("state")),
            r.getBoolean("reviewed"), r.getBoolean("strike"), r.getBoolean("forgiven"), r.getString("reason"),
            r.getLong("version"));
    }

    static <T> T one(List<T> values) {
        if (values.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND);
        }
        return values.getFirst();
    }
}
