package com.bannergress.backend.services.impl;

import com.bannergress.backend.entities.Place;
import com.bannergress.backend.entities.PlaceInformation;
import com.bannergress.backend.enums.PlaceType;
import com.bannergress.backend.services.GeocodingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

/**
 * Reverse Geocoding that divides the world in Northern/Southern Hemisphere, and
 * after that in Western/Eastern Hemisphere.
 */
@Service
@Transactional
@Profile("!googlemaps")
public class HemisphereGeocodingServiceImpl implements GeocodingService {
    @Autowired
    private EntityManager entityManager;

    @Override
    public List<Place> getPlaces(double latitude, double longitude) {
        Place country;
        Place belowCountry;
        if (latitude >= 0) {
            country = createCountry("N", "Northern Hemisphere");
        } else {
            country = createCountry("S", "Southern Hemisphere");
        }
        if (latitude >= 0 && longitude >= 0) {
            belowCountry = createAdministrativeArea("NE", "Eastern Hemisphere", "North-Eastern Region");
        } else if (latitude >= 0 && longitude < 0) {
            belowCountry = createAdministrativeArea("NW", "Western Hemisphere", "North-Western Region");
        } else if (latitude < 0 && longitude >= 0) {
            belowCountry = createAdministrativeArea("SE", "Eastern Hemisphere", "South-Eastern Region");
        } else {
            belowCountry = createAdministrativeArea("SW", "Western Hemisphere", "South-Western Region");
        }
        return List.of(country, belowCountry);
    }

    @Override
    public PlaceInformation getPlaceInformation(Place place, String language) {
        return place.getInformation().get(0);
    }

    private Place createCountry(String id, String longName) {
        return createPlace(PlaceType.country, id, longName, longName);
    }

    private Place createAdministrativeArea(String id, String longName, String formattedAddress) {
        return createPlace(PlaceType.administrative_area_level_1, id, longName, formattedAddress);
    }

    private Place createPlace(PlaceType placeType, String id, String longName, String formattedAddress) {
        Place place = entityManager.find(Place.class, id);
        if (place == null) {
            place = new Place();
            place.setId(id);
            place.setType(placeType);
            entityManager.persist(place);
            PlaceInformation placeInformation = new PlaceInformation();
            placeInformation.setPlace(place);
            placeInformation.setLanguageCode("en");
            placeInformation.setLongName(longName);
            placeInformation.setFormattedAddress(formattedAddress);
            placeInformation.setShortName(id);
            place.getInformation().add(placeInformation);
            entityManager.persist(placeInformation);
        }
        return place;
    }
}
