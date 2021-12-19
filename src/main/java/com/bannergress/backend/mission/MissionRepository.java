package com.bannergress.backend.mission;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.hibernate.LockOptions;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

import java.time.Instant;
import java.util.Optional;

/**
 * Repository for missions.
 */
public interface MissionRepository extends JpaRepository<Mission, String>, JpaSpecificationExecutor<Mission> {
    /** String value of {@link LockOptions#SKIP_LOCKED}. */
    public static final String SKIP_LOCKED = "-2";

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = AvailableSettings.JPA_LOCK_TIMEOUT, value = SKIP_LOCKED))
    Optional<Mission> findFirstByAuthorUpdateQueuedSinceLessThanOrderByAuthorUpdateQueuedSince(Instant maxQueuedSince);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = AvailableSettings.JPA_LOCK_TIMEOUT, value = SKIP_LOCKED))
    Optional<Mission> findFirstByStatusUpdateQueuedSinceLessThanOrderByStatusUpdateQueuedSince(Instant maxQueuedSince);
}
