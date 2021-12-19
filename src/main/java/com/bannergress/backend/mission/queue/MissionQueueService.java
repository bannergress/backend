package com.bannergress.backend.mission.queue;

import com.bannergress.backend.mission.Mission;

import java.util.Optional;

/**
 * Service for mission update request qeueuing.
 */
public interface MissionQueueService {
    void queueUpdateAuthor(Mission mission);

    void queueUpdateStatus(Mission mission);

    Optional<Mission> dequeueUpdateAuthor();

    Optional<Mission> dequeueUpdateStatus();

    void satisfyUpdateAuthor(Mission mission);

    void satisfyUpdateStatus(Mission mission);
}
