package com.bannergress.backend.services.impl;

import com.bannergress.backend.entities.Place;
import com.bannergress.backend.entities.PlaceInformation;
import com.bannergress.backend.enums.PlaceType;
import com.bannergress.backend.services.GeocodingService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Reverse Geocoding that divides the world in Northern/Southern Hemisphere, and
 * after that in Western/Eastern Hemisphere.
 */
@Service
@Profile("!googlemaps & !nominatim")
public class HemisphereGeocodingServiceImpl implements GeocodingService {
    @Override
    public Optional<Place> getPlaceHierarchy(double latitude, double longitude) {
        Place country = getCountry(latitude, longitude);
        Place belowCountry = getAdministrativeArea(latitude, longitude);
        belowCountry.setParentPlace(country);
        return Optional.of(belowCountry);
    }

    @Override
    public PlaceInformation getPlaceInformation(Place place, String language) {
        return place.getInformation().get(0);
    }

    private Place getCountry(double latitude, double longitude) {
        PlaceType type = PlaceType.country;
        if (latitude >= 0) {
            return createPlace(type, "N", "Northern Hemisphere", "Northern Hemisphere", 0, -180, 90, 180);
        } else {
            return createPlace(type, "S", "Southern Hemisphere", "Southern Hemisphere", -90, -180, 0, 180);
        }
    }

    private Place getAdministrativeArea(double latitude, double longitude) {
        PlaceType type = PlaceType.administrative_area_level_1;
        if (latitude >= 0 && longitude >= 0) {
            return createPlace(type, "NE", "Eastern Hemisphere", "North-Eastern Region", 0, 0, 90, 180);
        } else if (latitude >= 0 && longitude < 0) {
            return createPlace(type, "NW", "Western Hemisphere", "North-Western Region", 0, -180, 90, 0);
        } else if (latitude < 0 && longitude >= 0) {
            return createPlace(type, "SE", "Eastern Hemisphere", "South-Eastern Region", -90, 0, 0, 180);
        } else {
            return createPlace(type, "SW", "Western Hemisphere", "South-Western Region", -90, -180, 0, 0);
        }
    }

    private Place createPlace(PlaceType placeType, String id, String longName, String formattedAddress,
                              double minLatitude, double minLongitude, double maxLatitude, double maxLongitude) {
        Place place = new Place();
        place.setId(id);
        place.setType(placeType);
        place.setBoundaryMinLatitude(minLatitude);
        place.setBoundaryMinLongitude(minLongitude);
        place.setBoundaryMaxLatitude(maxLatitude);
        place.setBoundaryMaxLongitude(maxLongitude);
        PlaceInformation placeInformation = new PlaceInformation();
        placeInformation.setPlace(place);
        placeInformation.setLanguageCode("en");
        placeInformation.setLongName(longName);
        placeInformation.setFormattedAddress(formattedAddress);
        placeInformation.setShortName(id);
        place.getInformation().add(placeInformation);
        return place;
    }
}
