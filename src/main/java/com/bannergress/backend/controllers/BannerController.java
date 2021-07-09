package com.bannergress.backend.controllers;

import com.bannergress.backend.dto.BannerDto;
import com.bannergress.backend.entities.Banner;
import com.bannergress.backend.entities.PlaceInformation;
import com.bannergress.backend.enums.BannerSortOrder;
import com.bannergress.backend.exceptions.MissionAlreadyUsedException;
import com.bannergress.backend.security.Roles;
import com.bannergress.backend.services.BannerService;
import com.bannergress.backend.services.PlaceService;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.validation.constraints.Max;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

/**
 * REST endpoint for banners.
 */
@RestController
@Validated
public class BannerController {
    private static final Logger logger = LoggerFactory.getLogger(BannerController.class);

    private final BannerService bannerService;

    private final PlaceService placeService;

    public BannerController(final BannerService bannerService, final PlaceService placeService) {
        this.bannerService = bannerService;
        this.placeService = placeService;
    }

    /**
     * Lists banners.
     *
     * @param placeId              Place ID the banner belongs to.
     * @param minLatitude          Minimum latitude of the bounding box.
     * @param maxLatitude          Maximum latitude of the bounding box.
     * @param minLongitude         Minimum longitude of the bounding box.
     * @param maxLongitude         Maximum longitude of the bounding box.
     * @param query                Optional query string.
     * @param missionId            Optional ID of mission which has to be contained in banner.
     * @param onlyOfficialMissions Whether to only include official mission accounts.
     * @param author               Optional author of one of the banner missions.
     * @param orderBy              Sort order.
     * @param orderDirection       Sort direction.
     * @param offset               Offset of the first result.
     * @param limit                Maximum number of results.
     * @return Banners.
     */
    @GetMapping(value = "/bnrs")
    public ResponseEntity<List<BannerDto>> list(@RequestParam final Optional<String> placeId,
                                                @RequestParam final Optional<Double> minLatitude,
                                                @RequestParam final Optional<Double> maxLatitude,
                                                @RequestParam final Optional<Double> minLongitude,
                                                @RequestParam final Optional<Double> maxLongitude,
                                                @RequestParam final Optional<String> query,
                                                @RequestParam final Optional<String> missionId,
                                                @RequestParam(defaultValue = "false") final boolean onlyOfficialMissions,
                                                @RequestParam final Optional<String> author,
                                                @RequestParam final Optional<BannerSortOrder> orderBy,
                                                @RequestParam(defaultValue = "ASC") final Direction orderDirection,
                                                @RequestParam(defaultValue = "0") final int offset,
                                                @RequestParam(defaultValue = "20") @Max(100) final int limit) {
        int numberOfBounds = (minLatitude.isPresent() ? 1 : 0) + (maxLatitude.isPresent() ? 1 : 0)
            + (minLongitude.isPresent() ? 1 : 0) + (maxLongitude.isPresent() ? 1 : 0);
        if (numberOfBounds != 0 && numberOfBounds != 4) {
            return ResponseEntity.badRequest().build();
        }
        final Collection<Banner> banners = bannerService.find(placeId, minLatitude, maxLatitude, minLongitude,
            maxLongitude, query, missionId, onlyOfficialMissions, author, orderBy, orderDirection, offset, limit);
        return ResponseEntity.ok(banners.stream().map(this::toSummary).collect(Collectors.toUnmodifiableList()));
    }

    /**
     * Gets a banner with a specified ID.
     *
     * @param id
     * @return
     */
    @GetMapping("/bnrs/{id}")
    public ResponseEntity<BannerDto> get(@PathVariable final String id) {
        final Optional<Banner> banner = bannerService.findBySlugWithDetails(id);
        return ResponseEntity.of(banner.map(this::toDetails));
    }

    @RolesAllowed(Roles.CREATE_BANNER)
    @PostMapping("/bnrs")
    public ResponseEntity<BannerDto> post(@Valid @RequestBody BannerDto banner) throws MissionAlreadyUsedException {
        String id = bannerService.create(banner);
        return get(id);
    }

    @RolesAllowed(Roles.CREATE_BANNER)
    @PostMapping("/bnrs/preview")
    public BannerDto preview(@Valid @RequestBody BannerDto banner) throws MissionAlreadyUsedException {
        return toDetails(bannerService.generatePreview(banner));
    }

    /**
     * Updates the banner with the specified UUID.
     *
     * @param uuid   UUID.
     * @param banner Banner data.
     * @return Updated banner data.
     * @throws MissionAlreadyUsedException If a mission is already used by another banner.
     */
    @RolesAllowed(Roles.MANAGE_BANNERS)
    @PutMapping("/bnrs/{id}")
    public ResponseEntity<BannerDto> put(@PathVariable final String id, @Valid @RequestBody BannerDto banner)
        throws MissionAlreadyUsedException {
        bannerService.update(id, banner);
        return get(id);
    }

    /**
     * Deletes the banner with the specified UUID.
     *
     * @param uuid UUID.
     */
    @RolesAllowed(Roles.MANAGE_BANNERS)
    @DeleteMapping("/bnrs/{id}")
    public void delete(@PathVariable final String id) {
        bannerService.deleteBySlug(id);
    }

    @RolesAllowed(Roles.MANAGE_BANNERS)
    @PostMapping("/bnrs/recalculate")
    public void calculateAllBanners() throws InterruptedException, ExecutionException {
        List<UUID> uuids = bannerService.findAllUUIDs();
        ForkJoinPool pool = new ForkJoinPool(5);
        pool.submit(() -> uuids.parallelStream().forEach(uuid -> {
            try {
                bannerService.calculateBanner(uuid);
                logger.info("{}: Success", uuid);
            } catch (Exception e) {
                logger.error("{}: Failed to calculate", uuid, e);
            }
        })).get();
        pool.shutdown();
    }

    private BannerDto toSummary(Banner banner) {
        BannerDto dto = new BannerDto();
        dto.id = banner.getSlug();
        dto.title = banner.getTitle();
        dto.numberOfMissions = banner.getNumberOfMissions();
        dto.lengthMeters = banner.getLengthMeters();
        dto.startLatitude = banner.getStartLatitude();
        dto.startLongitude = banner.getStartLongitude();
        dto.picture = banner.getPicture() == null ? null : ("/bnrs/pictures/" + banner.getPicture().getHash());
        dto.width = banner.getWidth();
        Optional<PlaceInformation> placeInformation = placeService
            .getMostAccuratePlaceInformation(banner.getStartPlaces(), "en");
        if (placeInformation.isPresent()) {
            dto.startPlaceId = placeInformation.get().getPlace().getSlug();
            dto.formattedAddress = placeInformation.get().getFormattedAddress();
        }
        return dto;
    }

    private BannerDto toDetails(Banner banner) {
        BannerDto dto = toSummary(banner);
        dto.missions = Maps.transformValues(banner.getMissions(), MissionController::toDetails);
        dto.type = banner.getType();
        dto.description = banner.getDescription();
        return dto;
    }
}
