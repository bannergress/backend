package com.bannergress.backend.services.impl;

import com.bannergress.backend.entities.Place;
import com.bannergress.backend.services.GeocodingService;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class TestNominatimGeocoding {
    @Test
    void testReadNominatim() throws IOException {
        GeocodingService geocoding = new NominatimGeocodingServiceImpl("https://nominatim.openstreetmap.org/",
            Optional.empty());
        double latitude = 49.455556;
        double longitude = 10.4234469;
        Optional<Place> place = geocoding.getPlaceHierarchy(latitude, longitude);
        assertThat(place.isPresent());
        assertThat(place.get().getBoundaryMinLatitude()).isLessThanOrEqualTo(latitude);
        assertThat(place.get().getBoundaryMaxLatitude()).isGreaterThanOrEqualTo(latitude);
        assertThat(place.get().getBoundaryMinLongitude()).isLessThanOrEqualTo(longitude);
        assertThat(place.get().getBoundaryMaxLongitude()).isGreaterThanOrEqualTo(longitude);
    }
}
