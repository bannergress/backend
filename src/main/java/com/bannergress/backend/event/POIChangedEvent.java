package com.bannergress.backend.event;

import com.bannergress.backend.entities.POI;

/**
 * Event that fires when a POI is changed.
 */
public class POIChangedEvent {
    private final POI poi;

    public POIChangedEvent(POI poi) {
        this.poi = poi;
    }

    public POI getPoi() {
        return poi;
    }
}
