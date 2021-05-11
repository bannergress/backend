package com.bannergress.backend.services;

import com.bannergress.backend.entities.Place;
import com.bannergress.backend.entities.PlaceInformation;
import com.bannergress.backend.enums.PlaceSortOrder;
import com.bannergress.backend.enums.PlaceType;
import org.springframework.data.domain.Sort.Direction;

import java.util.Collection;
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
     * @param collapsePlaces  Collapse places that are nested within each other that have the same number of banners.
     * @return Found places.
     */
    Collection<Place> findUsedPlaces(Optional<String> parentPlaceSlug, Optional<String> queryString,
                                     Optional<PlaceType> type, PlaceSortOrder orderBy, Direction orderDirection,
                                     int offset, Optional<Integer> limit, boolean collapsePlaces);

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
     * @param place              Place.
     * @param languagePreference Language preference, as specified by the Accept-Language header.
     * @return Place information.
     */
    PlaceInformation getPlaceInformation(Place place, String languagePreference);

    /**
     * Retrieves all places a coordinate belongs to.
     *
     * @param latitude  Latitude.
     * @param longitude Longitude.
     * @return Multiple places with differing accuracy (i.e. one place with country accuracy, one place with locality accuracy, ...).
     */
    Collection<Place> getPlaces(double latitude, double longitude);

    /**
     * Gets localized information about the most specific place.
     *
     * @param places             Places.
     * @param languagePreference Language preference, as specified by the Accept-Language header.
     * @return Place information, if the list of places is not empty.
     */
    Optional<PlaceInformation> getMostAccuratePlaceInformation(Collection<Place> places, String languagePreference);
}
