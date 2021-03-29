package com.bannergress.backend.services;

import java.util.Collection;

import com.bannergress.backend.entities.Place;
import com.bannergress.backend.entities.PlaceInformation;

/** Service for Geocoding. */
public interface GeocodingService {
	/**
	 * Retrieves all places a coordinate belongs to.
	 * 
	 * @param latitude  Latitude.
	 * @param longitude Longitude.
	 * @return Multiple places with differing accuracy (i.e. one place with country accuracy, one place with locality accuracy, ...).
	 */
	Collection<Place> getPlaces(double latitude, double longitude);

	/**
	 * Retrieves human-readable information about a place.
	 * 
	 * @param place    Place
	 * @param language Preferred language.
	 * @return Information about the place.
	 */
	PlaceInformation getPlaceInformation(Place place, String language);
}
