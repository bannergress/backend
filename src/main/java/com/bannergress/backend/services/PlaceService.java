package com.bannergress.backend.services;

import com.bannergress.backend.entities.Place;
import com.bannergress.backend.entities.PlaceInformation;
import com.bannergress.backend.enums.PlaceType;

import java.util.Collection;
import java.util.Optional;

/**
 * Service for place-related task.
 */
public interface PlaceService {
    /**
     * Finds all places of a type that are used by at least one banner.
     *
     * @param parentPlaceId Optional ID of a parent place.
     * @param queryString   Optional query string to filter results.
     * @param type          Optional type of place to find.
     * @return Found places.
     */
    Collection<Place> findUsedPlaces(Optional<String> parentPlaceId, Optional<String> queryString,
                                     Optional<PlaceType> type);

    /**
     * Retrieves a place.
     *
     * @param id ID of the place.
     * @return Place.
     */
    Optional<Place> findPlaceById(String id);

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
