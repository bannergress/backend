package com.bannergress.backend.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * POI type.
 */
@Schema(enumAsRef = true)
public enum POIType {
    /**
     * Portal.
     */
    portal,
    /**
     * Field trip waypoint.
     */
    fieldTripWaypoint,
    /**
     * Unavailable.
     */
    unavailable
}
