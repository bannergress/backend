package com.bannergress.backend.services;

import org.locationtech.jts.geom.Point;

import java.time.ZoneId;

/**
 * Service for resolving time zones.
 */
public interface TimezoneService {
    /**
     * Retrieves the current timezone of a location.
     * @param point Location.
     * @return Time zone.
     */
    ZoneId getZone(Point point);
}
