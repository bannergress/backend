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
        Collection<Place> places = geocoding.getPlaces(49.455556, 10.4234469);
        assertThat(places.size()).isBetween(3, 4);
    }
}
