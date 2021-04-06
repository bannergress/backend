package com.bannergress.backend.controllers;

import com.bannergress.backend.dto.BannerDto;
import com.bannergress.backend.entities.Banner;
import com.bannergress.backend.services.BannerService;
import com.bannergress.backend.services.impl.PlaceServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static com.bannergress.backend.testutils.builder.BuilderMethods.a;
import static com.bannergress.backend.testutils.builder.DtoBuilder.$BannerDto;
import static com.bannergress.backend.testutils.builder.EntityBuilder.$Banner;
import static com.bannergress.backend.testutils.builder.JavatypeBuilder.$Double;
import static com.bannergress.backend.testutils.builder.JavatypeBuilder.$Long;
import static com.bannergress.backend.testutils.builder.JavatypeBuilder.$String;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BannerControllerTest {

    private final BannerService bannerService = mock(BannerService.class);

    private final BannerController testController = new BannerController(bannerService, new PlaceServiceImpl());

    @Test
    void list() {
        // WHEN
        final Optional<String> place = Optional.of(a($String()));
        final Banner banner = a($Banner());

        when(bannerService.find(eq(place), eq(Optional.empty()), eq(Optional.empty()), eq(Optional.empty()),
            eq(Optional.empty()), eq(Optional.empty()), any(), eq(0), anyInt())).thenReturn(List.of(banner));

        // THEN
        final ResponseEntity<List<BannerDto>> result = testController.list(place, Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.empty(), Optional.empty(), Direction.ASC, 0, 100);

        // VERIFY
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).hasSize(1);
        final var bannerDto = result.getBody().get(0);
        assertThat(bannerDto.id).isEqualTo(banner.getId());
        assertThat(bannerDto.numberOfMissions).isEqualTo(banner.getNumberOfMissions());
        assertThat(bannerDto.lengthMeters).isEqualTo(banner.getLengthMeters());
    }

    @Test
    void list_withBoundingBox() {
        // WHEN
        final Optional<Double> minLat = Optional.of(a($Double()));
        final Optional<Double> maxLat = Optional.of(a($Double()));
        final Optional<Double> minLong = Optional.of(a($Double()));
        final Optional<Double> maxLong = Optional.of(a($Double()));
        final Banner banner = a($Banner());

        when(bannerService.find(eq(Optional.empty()), eq(minLat), eq(maxLat), eq(minLong), eq(maxLong), any(), any(),
            eq(0), anyInt())).thenReturn(List.of(banner));

        // THEN
        final ResponseEntity<List<BannerDto>> result = testController.list(Optional.empty(), minLat, maxLat, minLong,
            maxLong, Optional.empty(), Direction.ASC, 0, 100);

        // VERIFY
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).hasSize(1);
        final var bannerDto = result.getBody().get(0);
        assertThat(bannerDto.id).isEqualTo(banner.getId());
        assertThat(bannerDto.numberOfMissions).isEqualTo(banner.getNumberOfMissions());
        assertThat(bannerDto.lengthMeters).isEqualTo(banner.getLengthMeters());
        assertThat(bannerDto.startLatitude).isEqualTo(banner.getStartLatitude());
        assertThat(bannerDto.startLongitude).isEqualTo(banner.getStartLongitude());
    }

    @Test
    void get() {
        // WHEN
        final long id = a($Long());
        final Banner banner = a($Banner());

        when(bannerService.findByIdWithDetails(id)).thenReturn(Optional.of(banner));

        // THEN
        final var response = testController.get(id);

        // VERIFY
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        final BannerDto bannerDto = response.getBody();
        assertThat(bannerDto).isNotNull();
        assertThat(bannerDto.id).isEqualTo(banner.getId());
        assertThat(bannerDto.numberOfMissions).isEqualTo(banner.getNumberOfMissions());
        assertThat(bannerDto.lengthMeters).isEqualTo(banner.getLengthMeters());
        assertThat(bannerDto.startLatitude).isEqualTo(banner.getStartLatitude());
        assertThat(bannerDto.startLongitude).isEqualTo(banner.getStartLongitude());
        assertThat(bannerDto.missions.get(0).id).isEqualTo(banner.getMissions().get(0).getId());
    }

    @Test
    void get_notFound() {
        // WHEN
        final long id = a($Long());

        when(bannerService.findByIdWithDetails(id)).thenReturn(Optional.empty());

        // THEN
        final var response = testController.get(id);

        // VERIFY
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void post() {
        // WHEN
        final BannerDto banner = a($BannerDto());
        final Banner savedBanner = a($Banner());

        when(bannerService.save(banner)).thenReturn(savedBanner.getId());
        when(bannerService.findByIdWithDetails(savedBanner.getId())).thenReturn(Optional.of(savedBanner));

        // THEN
        final var response = testController.post(banner);

        // VERIFY
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        final BannerDto bannerDto = response.getBody();
        assertThat(bannerDto).isNotNull();
        assertThat(bannerDto.id).isEqualTo(savedBanner.getId());
        assertThat(bannerDto.numberOfMissions).isEqualTo(savedBanner.getNumberOfMissions());
        assertThat(bannerDto.lengthMeters).isEqualTo(savedBanner.getLengthMeters());
        assertThat(bannerDto.startLatitude).isEqualTo(savedBanner.getStartLatitude());
        assertThat(bannerDto.startLongitude).isEqualTo(savedBanner.getStartLongitude());
        assertThat(bannerDto.missions.get(0).id).isEqualTo(savedBanner.getMissions().get(0).getId());
    }
}
