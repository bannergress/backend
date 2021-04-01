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
     * Lists banners inside a place.
     *
     * @param placeId Place ID.
     * @return Banners.
     */
    @GetMapping(value = "/banners", params = {"placeId"})
    public List<BannerDto> list(@RequestParam final String placeId) {
        final List<Banner> banners = bannerService.findByPlace(placeId, 0, MAX_RESULTS);
        return banners.stream().map(BannerController::toSummary).collect(Collectors.toUnmodifiableList());
    }

    /**
     * Lists banners inside a bounding box.
     *
     * @param minLatitude  Minimum latitude of the bounding box.
     * @param maxLatitude  Maximum latitude of the bounding box.
     * @param minLongitude Minimum longitude of the bounding box.
     * @param maxLongitude Maximum longitude of the bounding box.
     * @return Banners.
     */
    @GetMapping(value = "/banners", params = {"minLatitude", "maxLatitude", "minLongitude", "maxLongitude"})
    public List<BannerDto> list(@RequestParam final double minLatitude, @RequestParam final double maxLatitude,
                                @RequestParam final double minLongitude, @RequestParam final double maxLongitude) {
        final Collection<Banner> banners = bannerService.findByBounds(minLatitude, maxLatitude, minLongitude, maxLongitude, 0,
            MAX_RESULTS);
        return banners.stream().map(BannerController::toSummaryWithCoordinates).collect(Collectors.toUnmodifiableList());
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
