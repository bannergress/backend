package com.bannergress.backend.services;

import java.util.Collection;
import java.util.Optional;

import com.bannergress.backend.entities.Place;
import com.bannergress.backend.enums.PlaceType;

/** Service for place-related task. */
public interface PlaceService {
	/**
	 * Finds all places of a type that are used by at least one banner.
	 * 
	 * @param parentPlaceId Optional ID of a parent place.
	 * @param type          Type of place to find.
	 * @return Found places.
	 */
	Collection<Place> findUsedPlaces(Optional<String> parentPlaceId, PlaceType type);
}
