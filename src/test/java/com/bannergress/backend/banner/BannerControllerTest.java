package com.bannergress.backend.banner;

import com.bannergress.backend.banner.search.BannerSearchService;
import com.bannergress.backend.banner.settings.BannerSettingsServiceImpl;
import com.bannergress.backend.place.PlaceServiceImpl;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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
import static com.bannergress.backend.testutils.builder.JavatypeBuilder.$String;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BannerControllerTest {

    private final BannerService bannerService = mock(BannerService.class);
    private final BannerSearchService bannerSearchService = mock(BannerSearchService.class);

    private final BannerController testController = new BannerController(bannerService, new PlaceServiceImpl(),
        new BannerSettingsServiceImpl(), bannerSearchService);

    @Test
    void list() {
        // WHEN
        final Optional<String> place = Optional.of(a($String()));
        final Banner banner = fixPlaceInformation(a($Banner()));

        when(bannerSearchService.find(eq(place), eq(Optional.empty()), eq(Optional.empty()), eq(Optional.empty()),
            eq(Optional.empty()), eq(Optional.empty()), eq(false), eq(Optional.empty()), eq(false),
            eq(Optional.empty()), eq(Optional.empty()), eq(Optional.empty()), eq(Optional.empty()),
            eq(Optional.empty()), any(), eq(Optional.empty()), eq(Optional.empty()), eq(Optional.empty()),
            eq(Optional.empty()), eq(0), anyInt())).thenReturn(List.of(banner));

        // THEN
        final ResponseEntity<List<BannerDto>> result = testController.list(place, Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), false, Optional.empty(),
            Optional.empty(), Optional.empty(), Optional.empty(), Direction.ASC, Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.empty(), Optional.of(ImmutableSet.of(BannerDtoAttribute.id,
                BannerDtoAttribute.numberOfMissions, BannerDtoAttribute.lengthMeters)),
            0, 100, null, ImmutableList.of());

        // VERIFY
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).hasSize(1);
        final var bannerDto = result.getBody().get(0);
        assertThat(bannerDto.id).isEqualTo(banner.getCanonicalSlug());
        assertThat(bannerDto.numberOfMissions).isEqualTo(banner.getNumberOfMissions());
        assertThat(bannerDto.lengthMeters).isEqualTo(banner.getLengthMeters());
    }

    private static Banner fixPlaceInformation(final Banner banner) {
        // Fix back reference from place information to place
        banner.getStartPlaces()
            .forEach(place -> place.getInformation().forEach(information -> information.setPlace(place)));
        return banner;
    }

    @Test
    void list_withBoundingBox() {
        // WHEN
        final Optional<Double> minLat = Optional.of(a($Double()));
        final Optional<Double> maxLat = Optional.of(a($Double()));
        final Optional<Double> minLong = Optional.of(a($Double()));
        final Optional<Double> maxLong = Optional.of(a($Double()));
        final Banner banner = fixPlaceInformation(a($Banner()));

        when(bannerSearchService.find(eq(Optional.empty()), eq(minLat), eq(maxLat), eq(minLong), eq(maxLong), any(),
            eq(false), any(), eq(false), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), eq(0), anyInt()))
                .thenReturn(List.of(banner));

        // THEN
        final ResponseEntity<List<BannerDto>> result = testController.list(
            Optional.empty(), minLat, maxLat, minLong, maxLong, Optional.empty(), Optional.empty(), false,
            Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Direction.ASC, Optional.empty(),
            Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(ImmutableSet.of(BannerDtoAttribute.id,
                BannerDtoAttribute.numberOfMissions, BannerDtoAttribute.lengthMeters)),
            0, 100, null, ImmutableList.of());

        // VERIFY
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).hasSize(1);
        final var bannerDto = result.getBody().get(0);
        assertThat(bannerDto.id).isEqualTo(banner.getCanonicalSlug());
        assertThat(bannerDto.numberOfMissions).isEqualTo(banner.getNumberOfMissions());
        assertThat(bannerDto.lengthMeters).isEqualTo(banner.getLengthMeters());
    }

    @Test
    void get() {
        // WHEN
        final String slug = a($String());
        final Banner banner = fixPlaceInformation(a($Banner()));

        when(bannerService.findBySlugWithDetails(slug)).thenReturn(Optional.of(banner));

        // THEN
        final var response = testController.get(slug,
            Optional.of(ImmutableSet.of(BannerDtoAttribute.id, BannerDtoAttribute.numberOfMissions,
                BannerDtoAttribute.lengthMeters, BannerDtoAttribute.type, BannerDtoAttribute.missions)),
            null, ImmutableList.of());

        // VERIFY
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        final BannerDto bannerDto = response.getBody();
        assertThat(bannerDto).isNotNull();
        assertThat(bannerDto.id).isEqualTo(banner.getCanonicalSlug());
        assertThat(bannerDto.numberOfMissions).isEqualTo(banner.getNumberOfMissions());
        assertThat(bannerDto.lengthMeters).isEqualTo(banner.getLengthMeters());
        assertThat(bannerDto.type).isEqualTo(banner.getType());
        assertThat(bannerDto.missions.get(0).id).isEqualTo(banner.getMissions().get(0).getId());
    }

    @Test
    void get_notFound() {
        // WHEN
        final String slug = a($String());

        when(bannerService.findBySlugWithDetails(slug)).thenReturn(Optional.empty());

        // THEN
        final var response = testController.get(slug, Optional.of(ImmutableSet.of()), null, ImmutableList.of());

        // VERIFY
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void post() throws MissionAlreadyUsedException {
        // WHEN
        final BannerDto banner = a($BannerDto());
        final Banner savedBanner = fixPlaceInformation(a($Banner()));

        when(bannerService.create(banner)).thenReturn(savedBanner.getCanonicalSlug());
        when(bannerService.findBySlugWithDetails(savedBanner.getCanonicalSlug())).thenReturn(Optional.of(savedBanner));

        // THEN
        final var response = testController.post(banner, null, ImmutableList.of());

        // VERIFY
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        final BannerDto bannerDto = response.getBody();
        assertThat(bannerDto).isNotNull();
        assertThat(bannerDto.id).isEqualTo(savedBanner.getCanonicalSlug());
        assertThat(bannerDto.numberOfMissions).isEqualTo(savedBanner.getNumberOfMissions());
        assertThat(bannerDto.lengthMeters).isEqualTo(savedBanner.getLengthMeters());
        assertThat(bannerDto.type).isEqualTo(savedBanner.getType());
        assertThat(bannerDto.missions.get(0).id).isEqualTo(savedBanner.getMissions().get(0).getId());
    }
}
