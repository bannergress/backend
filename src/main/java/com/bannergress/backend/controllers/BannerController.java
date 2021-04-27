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
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.validation.constraints.Max;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST endpoint for banners.
 */
@RestController
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
     * @param placeId        Place ID the banner belongs to.
     * @param minLatitude    Minimum latitude of the bounding box.
     * @param maxLatitude    Maximum latitude of the bounding box.
     * @param minLongitude   Minimum longitude of the bounding box.
     * @param maxLongitude   Maximum longitude of the bounding box.
     * @param orderBy        Sort order.
     * @param orderDirection Sort direction.
     * @param offset         Offset of the first result.
     * @param limit          Maximum number of results.
     * @return Banners.
     */
    @GetMapping(value = "/bnrs")
    public ResponseEntity<List<BannerDto>> list(@RequestParam final Optional<String> placeId,
                                                @RequestParam final Optional<Double> minLatitude,
                                                @RequestParam final Optional<Double> maxLatitude,
                                                @RequestParam final Optional<Double> minLongitude,
                                                @RequestParam final Optional<Double> maxLongitude,
                                                @RequestParam final Optional<String> query,
                                                @RequestParam final Optional<BannerSortOrder> orderBy,
                                                @RequestParam(defaultValue = "ASC") final Direction orderDirection,
                                                @RequestParam(defaultValue = "0") final int offset,
                                                @RequestParam(defaultValue = "20") @Max(50) final int limit) {
        int numberOfBounds = (minLatitude.isPresent() ? 1 : 0) + (maxLatitude.isPresent() ? 1 : 0)
            + (minLongitude.isPresent() ? 1 : 0) + (maxLongitude.isPresent() ? 1 : 0);
        if (numberOfBounds != 0 && numberOfBounds != 4) {
            return ResponseEntity.badRequest().build();
        }
        final Collection<Banner> banners = bannerService.find(placeId, minLatitude, maxLatitude, minLongitude,
            maxLongitude, query, orderBy, orderDirection, offset, limit);
        return ResponseEntity.ok(banners.stream().map(this::toSummary).collect(Collectors.toUnmodifiableList()));
    }

    /**
     * Gets a banner with a specified ID.
     *
     * @param id
     * @return
     */
    @GetMapping("/bnrs/{uuid}")
    public ResponseEntity<BannerDto> get(@PathVariable final UUID uuid) {
        final Optional<Banner> banner = bannerService.findByUuidWithDetails(uuid);
        return ResponseEntity.of(banner.map(this::toDetails));
    }

    @RolesAllowed(Roles.CREATE_BANNER)
    @PostMapping("/bnrs")
    public ResponseEntity<BannerDto> post(@Valid @RequestBody BannerDto banner) throws MissionAlreadyUsedException {
        UUID uuid = bannerService.create(banner);
        return get(uuid);
    }

    /**
     * Updates the banner with the specified UUID.
     *
     * @param uuid   UUID.
     * @param banner Banner data.
     * @return Updated banner data.
     */
    @RolesAllowed(Roles.MANAGE_BANNERS)
    @PutMapping("/bnrs/{uuid}")
    public ResponseEntity<BannerDto> put(@PathVariable final UUID uuid, @Valid @RequestBody BannerDto banner) {
        bannerService.update(uuid, banner);
        return get(uuid);
    }

    /**
     * Deletes the banner with the specified UUID.
     *
     * @param uuid UUID.
     */
    @RolesAllowed(Roles.MANAGE_BANNERS)
    @DeleteMapping("/bnrs/{uuid}")
    public void delete(@PathVariable final UUID uuid) {
        bannerService.deleteByUuid(uuid);
    }

    @RolesAllowed(Roles.MANAGE_BANNERS)
    @PostMapping("/bnrs/recalculate")
    public void calculateAllBanners() {
        bannerService.calculateAllBanners();
    }

    private BannerDto toSummary(Banner banner) {
        BannerDto dto = new BannerDto();
        dto.uuid = banner.getUuid();
        dto.title = banner.getTitle();
        dto.numberOfMissions = banner.getNumberOfMissions();
        dto.lengthMeters = banner.getLengthMeters();
        dto.startLatitude = banner.getStartLatitude();
        dto.startLongitude = banner.getStartLongitude();
        dto.picture = "/bnrs/pictures/" + banner.getPicture().getHash();
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
        dto.type = banner.getType();
        return dto;
    }
}
