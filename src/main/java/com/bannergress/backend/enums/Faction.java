package com.bannergress.backend.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents an Ingress faction.
 */
@Schema(enumAsRef = true)
public enum Faction {
    /**
     * Enlightened.
     */
    enlightened,
    /**
     * Resistance.
     */
    resistance
}
