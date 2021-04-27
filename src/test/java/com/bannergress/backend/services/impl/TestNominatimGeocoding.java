package com.bannergress.backend.services.impl;

import com.bannergress.backend.entities.Place;
import com.bannergress.backend.services.GeocodingService;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestNominatimGeocoding {
    @Test
    void testReadNominatim() throws IOException {
        final EntityManager entityManager = mock(EntityManager.class);
        when(entityManager.find(any(), any())).thenReturn(null);

        GeocodingService geocoding = new NominatimGeocodingServiceImpl("https://nominatim.openstreetmap.org/",
            Optional.empty(), entityManager);
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
