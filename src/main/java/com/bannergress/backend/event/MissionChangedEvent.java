package com.bannergress.backend.event;

import com.bannergress.backend.entities.Mission;

/**
 * Event that fires when a mission is changed.
 */
public class MissionChangedEvent {
    private final Mission mission;

    public MissionChangedEvent(Mission mission) {
        this.mission = mission;
    }

    public Mission getMission() {
        return mission;
    }
}
