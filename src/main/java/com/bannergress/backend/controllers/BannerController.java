package com.bannergress.backend.controllers;

import com.bannergress.backend.dto.BannerDto;
import com.bannergress.backend.entities.Banner;
import com.bannergress.backend.entities.PlaceInformation;
import com.bannergress.backend.enums.BannerSortOrder;
import com.bannergress.backend.services.BannerService;
import com.bannergress.backend.services.PlaceService;
import com.google.common.collect.Maps;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST endpoint for banners.
 */
@RestController
@CrossOrigin
public class BannerController {

    private final BannerService bannerService;

    private final PlaceService placeService;

    public BannerController(final BannerService bannerService, final PlaceService placeService) {
        this.bannerService = bannerService;
        this.placeService = placeService;
    }

    /**
     * Lists banners.
     *
     * @param placeId      Place ID the banner belongs to.
     * @param minLatitude  Minimum latitude of the bounding box.
     * @param maxLatitude  Maximum latitude of the bounding box.
     * @param minLongitude Minimum longitude of the bounding box.
     * @param maxLongitude Maximum longitude of the bounding box.
     * @param sortBy       Sort order.
     * @param dir          Sort direction.
     * @param offset       Offset of the first result.
     * @param limit        Maximum number of results.
     * @return Banners.
     */
    @GetMapping(value = "/banners")
    public ResponseEntity<List<BannerDto>> list(@RequestParam final Optional<String> placeId,
                                                @RequestParam final Optional<Double> minLatitude,
                                                @RequestParam final Optional<Double> maxLatitude,
                                                @RequestParam final Optional<Double> minLongitude,
                                                @RequestParam final Optional<Double> maxLongitude,
                                                @RequestParam final Optional<BannerSortOrder> sortBy,
                                                @RequestParam(defaultValue = "ASC") final Direction dir,
                                                @RequestParam(defaultValue = "0") final int offset,
                                                @RequestParam(defaultValue = "20") @Max(50) final int limit) {
        int numberOfBounds = (minLatitude.isPresent() ? 1 : 0) + (maxLatitude.isPresent() ? 1 : 0)
            + (minLongitude.isPresent() ? 1 : 0) + (maxLongitude.isPresent() ? 1 : 0);
        if (numberOfBounds != 0 && numberOfBounds != 4) {
            return ResponseEntity.badRequest().build();
        }
        final Collection<Banner> banners = bannerService.find(placeId, minLatitude, maxLatitude, minLongitude,
            maxLongitude, sortBy, dir, offset, limit);
        return ResponseEntity.ok(banners.stream().map(this::toSummary).collect(Collectors.toUnmodifiableList()));
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
        return ResponseEntity.of(banner.map(this::toDetails));
    }

    @PostMapping("/banners")
    public ResponseEntity<BannerDto> post(@Valid @RequestBody BannerDto banner) {
        long id = bannerService.save(banner);
        return get(id);
    }

    private BannerDto toSummary(Banner banner) {
        BannerDto dto = new BannerDto();
        dto.id = banner.getId();
        dto.title = banner.getTitle();
        dto.numberOfMissions = banner.getNumberOfMissions();
        dto.lengthMeters = banner.getLengthMeters();
        dto.startLatitude = banner.getStartLatitude();
        dto.startLongitude = banner.getStartLongitude();
        Optional<PlaceInformation> placeInformation = placeService
            .getMostAccuratePlaceInformation(banner.getStartPlaces(), "en");
        if (placeInformation.isPresent()) {
            dto.formattedAddress = placeInformation.get().getFormattedAddress();
        }
        return dto;
    }

    private BannerDto toDetails(Banner banner) {
        BannerDto dto = toSummary(banner);
        dto.missions = Maps.transformValues(banner.getMissions(), MissionController::toDetails);
        return dto;
    }
}
