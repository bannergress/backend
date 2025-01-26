package com.bannergress.backend.mission.creator;

/**
 * Status of a mission in the mission creator.
 */
public enum CreatorMissionStatus {
    DRAFT,
    /**
     * Submitted for review.
     */
    SUBMITTED,
    /**
     * Published in-game.
     */
    PUBLISHED,
    /**
     * Offline.
     */
    DISABLED
}
