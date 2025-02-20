package com.bannergress.backend.place;

import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Sort.Direction;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Service for place-related task.
 */
public interface PlaceService {
    /**
     * Finds all places of a type that are used by at least one banner.
     *
     * @param parentPlaceSlug Optional slug of a parent place.
     * @param queryString     Optional query string to filter results.
     * @param type            Optional type of place to find.
     * @param orderBy         Sort order.
     * @param orderDirection  Sort direction.
     * @param offset          Offset of the first result.
     * @param limit           Maximum number of results.
     * @return Found places.
     */
    Collection<Place> findUsedPlaces(Optional<String> parentPlaceSlug, Optional<String> queryString,
                                     Optional<PlaceType> type, PlaceSortOrder orderBy, Direction orderDirection,
                                     int offset, Optional<Integer> limit);

    /**
     * Retrieves a place.
     *
     * @param slug (ID which is suitable for use in URLs) of the place.
     * @return Place.
     */
    Optional<Place> findPlaceBySlug(String slug);

    /**
     * Gets localized information about a place.
     *
     * @param place            Place.
     * @param languagePriority Language priority list.
     * @return Place information.
     */
    PlaceInformation getPlaceInformation(Place place, List<Locale.LanguageRange> languagePriorityList);

    /**
     * Retrieves all places a coordinate belongs to.
     *
     * @param point Point.
     * @return Multiple places with differing accuracy (i.e. one place with country accuracy, one place with locality accuracy, ...).
     */
    Collection<Place> getPlaces(Point point);

    /**
     * Gets localized information about the most specific place.
     *
     * @param places           Places.
     * @param languagePriority Language priority list.
     * @return Place information, if the list of places is not empty.
     */
    Optional<PlaceInformation> getMostAccuratePlaceInformation(Collection<Place> places,
                                                               List<Locale.LanguageRange> languagePriorityList);

    /**
     * Updates information for a set of places.
     *
     * @param places Places.
     */
    void updatePlaces(Collection<Place> places);

    /**
     * Updates information for all places.
     */
    void updateAllPlaces();
}
