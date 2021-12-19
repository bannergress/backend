package com.bannergress.backend.services;

import com.bannergress.backend.entities.Mission;

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
