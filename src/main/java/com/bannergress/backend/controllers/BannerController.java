package com.bannergress.backend.controllers;

import com.bannergress.backend.dto.BannerDto;
import com.bannergress.backend.entities.Banner;
import com.bannergress.backend.services.BannerService;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Optional;

/**
 * REST endpoint for banners.
 */
@RestController
public class BannerController {
    @Autowired
    BannerService bannerService;

    private static final int MAX_RESULTS = 1000;

    /**
     * Lists banners inside a place.
     *
     * @param placeId Place ID.
     * @return Banners.
     */
    @GetMapping(value = "/banners", params = {"placeId"})
    public Collection<BannerDto> list(@RequestParam String placeId) {
        Collection<Banner> banners = bannerService.findByPlace(placeId, 0, MAX_RESULTS);
        return Collections2.transform(banners, BannerController::toSummary);
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
    public Collection<BannerDto> list(@RequestParam double minLatitude, @RequestParam double maxLatitude,
                                      @RequestParam double minLongitude, @RequestParam double maxLongitude) {
        Collection<Banner> banners = bannerService.findByBounds(minLatitude, maxLatitude, minLongitude, maxLongitude, 0,
            MAX_RESULTS);
        return Collections2.transform(banners, BannerController::toSummaryWithCoordinates);
    }

    /**
     * Gets a banner with a specified ID.
     *
     * @param id
     * @return
     */
    @GetMapping("/banners/{id}")
    public BannerDto get(@PathVariable long id) {
        Optional<Banner> banner = bannerService.findByIdWithDetails(id);
        if (banner.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else {
            return toDetails(banner.get());
        }
    }

    @PostMapping("/banners")
    public BannerDto post(@Valid @RequestBody BannerDto banner) {
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
