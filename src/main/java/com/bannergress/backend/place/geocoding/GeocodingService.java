package com.bannergress.backend.place.geocoding;

import com.bannergress.backend.place.Place;
import com.bannergress.backend.place.PlaceInformation;

import java.util.Set;

/**
 * Service for Geocoding.
 */
public interface GeocodingService {
    /**
     * Retrieves the places a coordinate belongs to.
     *
     * @param latitude  Latitude.
     * @param longitude Longitude.
     * @return Places.
     */
    Set<Place> getPlaces(double latitude, double longitude);

    /**
     * Retrieves human-readable information about a place.
     *
     * @param place    Place
     * @param language Preferred language.
     * @return Information about the place.
     */
    PlaceInformation getPlaceInformation(Place place, String language);
}
