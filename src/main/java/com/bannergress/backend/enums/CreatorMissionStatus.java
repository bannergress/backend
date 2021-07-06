package com.bannergress.backend.enums;

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
