package com.bannergress.backend.services;

import com.bannergress.backend.entities.Place;
import com.bannergress.backend.entities.PlaceInformation;

import java.util.Optional;

/**
 * Service for Geocoding.
 */
public interface GeocodingService {
    /**
     * Retrieves the hierarchy of places a coordinate belongs to.
     *
     * @param latitude  Latitude.
     * @param longitude Longitude.
     * @return Place hierarchy.
     */
    Optional<Place> getPlaceHierarchy(double latitude, double longitude);

    /**
     * Retrieves human-readable information about a place.
     *
     * @param place    Place
     * @param language Preferred language.
     * @return Information about the place.
     */
    PlaceInformation getPlaceInformation(Place place, String language);
}
