package com.bannergress.backend.enums;

import com.bannergress.backend.entities.Banner;

/**
 * Possible sort orders for banners.
 */
public enum BannerSortOrder {
    /** Order by {@link Banner#getCreated}. */
    created,
    /** Order by {@link Banner#getTitle}. */
    title,
    /** Order by {@link Banner#getNumberOfMissions}. */
    numberOfMissions,
    /** Order by {@link Banner#getLengthMeters}. */
    lengthMeters,
    /** Order by {@link BannerSettings#listAdded}. */
    listAdded,
    /** Order by distance between {@link Banner#getStartPoint} and a given point. */
    proximityStartPoint
}
