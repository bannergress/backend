package com.bannergress.backend.banner.search;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Possible sort orders for banners.
 */
@Schema(enumAsRef = true)
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
    proximityStartPoint,
    /** Order by relevance of the search result. */
    relevance
}
