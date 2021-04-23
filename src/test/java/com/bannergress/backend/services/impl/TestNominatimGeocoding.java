package com.bannergress.backend.services.impl;

import com.bannergress.backend.entities.Place;
import com.bannergress.backend.services.GeocodingService;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class TestNominatimGeocoding {
    @Test
    void testReadNominatim() throws IOException {
        GeocodingService geocoding = new NominatimGeocodingServiceImpl("https://nominatim.openstreetmap.org/",
            Optional.empty());
        double latitude = 49.455556;
        double longitude = 10.4234469;
        Collection<Place> places = geocoding.getPlaces(latitude, longitude);
        assertThat(places.size()).isBetween(3, 4);
        Place place = places.iterator().next();
        assertThat(place.getBoundaryMinLatitude()).isLessThanOrEqualTo(latitude);
        assertThat(place.getBoundaryMaxLatitude()).isGreaterThanOrEqualTo(latitude);
        assertThat(place.getBoundaryMinLongitude()).isLessThanOrEqualTo(longitude);
        assertThat(place.getBoundaryMaxLongitude()).isGreaterThanOrEqualTo(longitude);
    }
}
