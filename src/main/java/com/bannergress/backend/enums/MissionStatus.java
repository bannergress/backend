package com.bannergress.backend.enums;

/**
 * Mission status.
 */
public enum MissionStatus {
    /**
     * Mission has been submitted to Niantic for review.
     */
    submitted,
    /**
     * Mission has been accepted by Niantic and is online.
     */
    published,
    /**
     * Mission has been disabled by Niantic or by the author.
     */
    disabled
}
