package com.bannergress.backend.comment;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import static com.bannergress.backend.comment.CommentModels.*;
import static org.springframework.http.HttpStatus.*;

/** Durable at-least-once delivery with expiring fenced leases and bounded retries. */
@Service
@Transactional
public class CommentOutbox {
    private final CommentStore store;
    private final CommentSettings settings;

    public CommentOutbox(CommentStore store, CommentSettings settings) {
        this.store = store;
        this.settings = settings;
    }

    public List<Delivery> claim(int limit) {
        if (limit < 1 || limit > 50) {
            throw new ResponseStatusException(BAD_REQUEST);
        }
        store.jdbc.update("""
            UPDATE comment_outbox SET state = 'DEAD', lease_token = NULL, lease_until = NULL
            WHERE environment = ? AND state = 'QUEUED' AND attempts >= 10
            AND (lease_until IS NULL OR lease_until < clock_timestamp())
            """, settings.environment());
        UUID token = UUID.randomUUID();
        return store.jdbc.query("""
            WITH selected AS (
                SELECT id FROM comment_outbox WHERE environment = ? AND state = 'QUEUED'
                AND available_at <= clock_timestamp() AND attempts < 10
                AND (lease_until IS NULL OR lease_until < clock_timestamp())
                ORDER BY created_at, id LIMIT ? FOR UPDATE SKIP LOCKED
            )
            UPDATE comment_outbox o SET lease_token = ?, lease_until = clock_timestamp() + interval '2 minutes',
                attempts = attempts + 1 FROM selected WHERE o.id = selected.id RETURNING o.*
            """, (r, n) -> new Delivery(r.getObject("id", UUID.class), r.getObject("comment_id", UUID.class),
                r.getObject("author_id", UUID.class), r.getString("event_type"), r.getString("environment"),
                r.getObject("lease_token", UUID.class), r.getTimestamp("lease_until").toInstant(), r.getInt("attempts")),
            settings.environment(), limit, token);
    }

    public void acknowledge(UUID id, Acknowledge request) {
        var ids = request.telegramMessageIds();
        if (ids == null || ids.isEmpty() || ids.size() > 100
            || ids.stream().anyMatch(s -> s == null || !s.matches("[0-9]+:[0-9]+|-[0-9]+:[0-9]+"))) {
            throw new ResponseStatusException(BAD_REQUEST, "Expected chat:message numeric references");
        }
        // A replay of the same acknowledgement is safe; a stale lease can never acknowledge a newer delivery.
        int updated = store.jdbc.update(connection -> {
            var statement = connection.prepareStatement("""
                UPDATE comment_outbox SET state = 'DELIVERED', telegram_message_ids = ?
                WHERE id = ? AND environment = ? AND lease_token = ?
                AND ((state = 'QUEUED' AND lease_until > clock_timestamp())
                    OR (state = 'DELIVERED' AND telegram_message_ids = ?))
                """);
            statement.setArray(1, connection.createArrayOf("text", ids.toArray(String[]::new)));
            statement.setObject(2, id);
            statement.setString(3, settings.environment());
            statement.setObject(4, request.leaseToken());
            statement.setArray(5, connection.createArrayOf("text", ids.toArray(String[]::new)));
            return statement;
        });
        requireUpdate(updated);
    }

    public void retry(UUID id, UUID token) {
        int updated = store.jdbc.update("""
            UPDATE comment_outbox SET state = CASE WHEN attempts >= 10 THEN 'DEAD' ELSE 'QUEUED' END,
                available_at = clock_timestamp() + least(3600, power(2, attempts)) * interval '1 second',
                lease_token = NULL, lease_until = NULL
            WHERE id = ? AND environment = ? AND state = 'QUEUED' AND lease_token = ?
                AND lease_until > clock_timestamp()
            """, id, settings.environment(), token);
        requireUpdate(updated);
    }

    public void requeue(UUID id) {
        requireUpdate(store.jdbc.update("""
            UPDATE comment_outbox SET state = 'QUEUED', attempts = 0, available_at = clock_timestamp(),
                lease_token = NULL, lease_until = NULL WHERE id = ? AND environment = ? AND state = 'DEAD'
            """, id, settings.environment()));
    }

    @Transactional(readOnly = true)
    public List<UUID> dead(int offset, int limit) {
        CommentService.pageBounds(offset, limit);
        return store.jdbc.query("""
            SELECT id FROM comment_outbox WHERE environment = ? AND state = 'DEAD'
            ORDER BY created_at, id LIMIT ? OFFSET ?
            """, (r, n) -> r.getObject("id", UUID.class), settings.environment(), limit, offset);
    }

    @Transactional(readOnly = true)
    public QueueStats stats() {
        return store.jdbc.queryForObject("""
            SELECT count(*) FILTER (WHERE state = 'QUEUED') AS queued,
                count(*) FILTER (WHERE state = 'DEAD') AS dead,
                min(created_at) FILTER (WHERE state = 'QUEUED') AS oldest
            FROM comment_outbox WHERE environment = ?
            """, (r, n) -> {
                Timestamp oldest = r.getTimestamp("oldest");
                return new QueueStats(r.getLong("queued"), r.getLong("dead"), oldest == null ? null : oldest.toInstant());
            }, settings.environment());
    }

    private void requireUpdate(int changed) {
        if (changed != 1) {
            throw new ResponseStatusException(CONFLICT, "Unknown, stale or expired delivery lease/state");
        }
    }
}
