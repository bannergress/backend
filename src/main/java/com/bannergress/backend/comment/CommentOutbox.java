package com.bannergress.backend.comment;

import jakarta.persistence.LockModeType;
import org.hibernate.LockMode;
import org.hibernate.query.SelectionQuery;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.bannergress.backend.comment.CommentModels.*;
import static org.springframework.http.HttpStatus.*;

/** Durable at-least-once delivery with Hibernate row locks, expiring fenced leases and bounded retries. */
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
        Instant now = store.now();
        var exhaustedQuery = store.entities().createQuery("from CommentOutboxEvent where environment = :env and state = 'QUEUED' and attempts >= 10 and (leaseUntil is null or leaseUntil < :now)", CommentOutboxEvent.class)
            .setParameter("env", settings.environment()).setParameter("now", now);
        var exhausted = skipLocked(exhaustedQuery).getResultList();
        exhausted.forEach(e -> {
            e.state = "DEAD";
            e.leaseToken = null;
            e.leaseUntil = null;
        });
        var eventsQuery = store.entities().createQuery("from CommentOutboxEvent where environment = :env and state = 'QUEUED' and attempts < 10 and availableAt <= :now and (leaseUntil is null or leaseUntil < :now) order by createdAt, id", CommentOutboxEvent.class)
            .setParameter("env", settings.environment()).setParameter("now", now).setMaxResults(limit);
        var events = skipLocked(eventsQuery).getResultList();
        return events.stream().map(e -> {
            e.leaseToken = UUID.randomUUID();
            e.leaseUntil = now.plusSeconds(120);
            e.attempts++;
            return new Delivery(e.id, e.comment, e.author, e.eventType, e.environment, e.leaseToken, e.leaseUntil, e.attempts);
        }).toList();
    }

    private <T> TypedQuery<T> skipLocked(TypedQuery<T> query) {
        query.unwrap(SelectionQuery.class).setHibernateLockMode(LockMode.UPGRADE_SKIPLOCKED);
        return query;
    }

    private CommentOutboxEvent locked(UUID id) {
        var event = store.entities().find(CommentOutboxEvent.class, id, LockModeType.PESSIMISTIC_WRITE);
        require(event != null && event.environment.equals(settings.environment()));
        return event;
    }

    private void lease(CommentOutboxEvent event, UUID token) {
        require(event.state.equals("QUEUED") && token != null && token.equals(event.leaseToken)
            && event.leaseUntil != null && event.leaseUntil.isAfter(store.now()));
    }

    public void acknowledge(UUID id, Acknowledge request) {
        var ids = request.telegramMessageIds();
        if (ids == null || ids.isEmpty() || ids.size() > 100
            || ids.stream().anyMatch(s -> s == null || !s.matches("[0-9]+:[0-9]+|-[0-9]+:[0-9]+"))) {
            throw new ResponseStatusException(BAD_REQUEST, "Expected chat:message numeric references");
        }
        var event = locked(id);
        if (event.state.equals("DELIVERED") && event.leaseToken.equals(request.leaseToken())
            && ids.equals(Arrays.asList(event.telegramMessageIds))) {
            return;
        }
        lease(event, request.leaseToken());
        event.state = "DELIVERED";
        event.telegramMessageIds = ids.toArray(String[]::new);
    }

    public void retry(UUID id, UUID token) {
        var event = locked(id);
        lease(event, token);
        event.state = event.attempts >= 10 ? "DEAD" : "QUEUED";
        event.availableAt = store.now().plusSeconds(Math.min(3600, 1L << event.attempts));
        event.leaseToken = null;
        event.leaseUntil = null;
    }

    public void requeue(UUID id) {
        var event = locked(id);
        require(event.state.equals("DEAD"));
        event.state = "QUEUED";
        event.attempts = 0;
        event.availableAt = store.now();
        event.leaseToken = null;
        event.leaseUntil = null;
    }

    @Transactional(readOnly = true)
    public List<UUID> dead(int offset, int limit) {
        CommentService.pageBounds(offset, limit);
        return store.entities().createQuery("select id from CommentOutboxEvent where environment = :env and state = 'DEAD' order by createdAt, id", UUID.class)
            .setParameter("env", settings.environment()).setFirstResult(offset).setMaxResults(limit).getResultList();
    }

    @Transactional(readOnly = true)
    public QueueStats stats() {
        long queued = count("QUEUED");
        long dead = count("DEAD");
        Instant oldest = store.entities().createQuery("select min(createdAt) from CommentOutboxEvent where environment = :env and state = 'QUEUED'", Instant.class)
            .setParameter("env", settings.environment()).getSingleResult();
        return new QueueStats(queued, dead, oldest);
    }

    private long count(String state) {
        return store.entities().createQuery("select count(e) from CommentOutboxEvent e where e.environment = :env and e.state = :state", Long.class)
            .setParameter("env", settings.environment()).setParameter("state", state).getSingleResult();
    }

    private void require(boolean valid) {
        if (!valid) {
            throw new ResponseStatusException(CONFLICT, "Unknown, stale or expired delivery lease/state");
        }
    }
}
