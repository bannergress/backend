package com.bannergress.backend.controllers;

import com.bannergress.backend.dto.BannerDto;
import com.bannergress.backend.entities.Banner;
import com.bannergress.backend.services.BannerService;
import com.google.common.collect.Maps;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST endpoint for banners.
 */
@RestController
public class BannerController {

    private final BannerService bannerService;

    private static final int MAX_RESULTS = 1000;

    public BannerController(final BannerService bannerService) {
        this.bannerService = bannerService;
    }

    /**
     * Lists banners.
     *
     * @param placeId      Place ID the banner belongs to.
     * @param minLatitude  Minimum latitude of the bounding box.
     * @param maxLatitude  Maximum latitude of the bounding box.
     * @param minLongitude Minimum longitude of the bounding box.
     * @param maxLongitude Maximum longitude of the bounding box.
     * @return Banners.
     */
    @GetMapping(value = "/banners")
    public ResponseEntity<List<BannerDto>> list(@RequestParam final Optional<String> placeId,
                                                @RequestParam final Optional<Double> minLatitude,
                                                @RequestParam final Optional<Double> maxLatitude,
                                                @RequestParam final Optional<Double> minLongitude,
                                                @RequestParam final Optional<Double> maxLongitude) {
        int numberOfBounds = (minLatitude.isPresent() ? 1 : 0) + (maxLatitude.isPresent() ? 1 : 0)
            + (minLongitude.isPresent() ? 1 : 0) + (maxLongitude.isPresent() ? 1 : 0);
        if (numberOfBounds != 0 && numberOfBounds != 4) {
            return ResponseEntity.badRequest().build();
        }
        final Collection<Banner> banners = bannerService.find(placeId, minLatitude, maxLatitude, minLongitude,
            maxLongitude, 0, MAX_RESULTS);
        return ResponseEntity.ok(
            banners.stream().map(BannerController::toSummaryWithCoordinates).collect(Collectors.toUnmodifiableList()));
    }

    /**
     * Gets a banner with a specified ID.
     *
     * @param id
     * @return
     */
    @GetMapping("/banners/{id}")
    public ResponseEntity<BannerDto> get(@PathVariable final long id) {
        final Optional<Banner> banner = bannerService.findByIdWithDetails(id);
        return ResponseEntity.of(banner.map(BannerController::toDetails));
    }

    @PostMapping("/banners")
    public ResponseEntity<BannerDto> post(@Valid @RequestBody BannerDto banner) {
        long id = bannerService.save(banner);
        return get(id);
    }

    private static BannerDto toSummary(Banner banner) {
        BannerDto dto = new BannerDto();
        dto.id = banner.getId();
        dto.title = banner.getTitle();
        dto.numberOfMissions = banner.getNumberOfMissions();
        dto.lengthMeters = banner.getLengthMeters();
        return dto;
    }

    private static BannerDto toSummaryWithCoordinates(Banner banner) {
        BannerDto dto = toSummary(banner);
        dto.startLatitude = banner.getStartLatitude();
        dto.startLongitude = banner.getStartLongitude();
        return dto;
    }

    private static BannerDto toDetails(Banner banner) {
        BannerDto dto = toSummaryWithCoordinates(banner);
        dto.missions = Maps.transformValues(banner.getMissions(), MissionController::toDetails);
        return dto;
    }
}
