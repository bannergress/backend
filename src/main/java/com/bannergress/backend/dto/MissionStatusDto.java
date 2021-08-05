package com.bannergress.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.time.Instant;

/**
 * Transports status information for a single mission.
 */
@JsonInclude(Include.NON_NULL)
public class MissionStatusDto {
    /**
     * Timestamp when the mission summary was last updated.
     */
    public Instant latestUpdateSummary;

    /**
     * Timestamp when the mission details were last updated.
     */
    public Instant latestUpdateDetails;
}
