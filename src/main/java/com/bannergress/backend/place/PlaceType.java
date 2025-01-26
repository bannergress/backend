package com.bannergress.backend.place;

/**
 * Type of place.
 */
public enum PlaceType {
    /**
     * National political entity.
     */
    country,
    /**
     * Indicates a first-order civil entity below the country level.
     */
    administrative_area_level_1,
    /**
     * Indicates a second-order civil entity below the country level.
     */
    administrative_area_level_2,
    /**
     * Indicates a third-order civil entity below the country level.
     */
    administrative_area_level_3,
    /**
     * Indicates a fourth-order civil entity below the country level.
     */
    administrative_area_level_4,
    /**
     * Indicates a fifth-order civil entity below the country level.
     */
    administrative_area_level_5,
    /**
     * Indicates an incorporated city or town political entity.
     */
    locality
}
