package com.bannergress.backend.controllers;

import com.bannergress.backend.dto.BannerDto;
import com.bannergress.backend.dto.BannerSettingsDto;
import com.bannergress.backend.entities.Banner;
import com.bannergress.backend.entities.BannerSettings;
import com.bannergress.backend.entities.PlaceInformation;
import com.bannergress.backend.enums.BannerListType;
import com.bannergress.backend.enums.BannerSortOrder;
import com.bannergress.backend.exceptions.MissionAlreadyUsedException;
import com.bannergress.backend.security.Roles;
import com.bannergress.backend.services.BannerService;
import com.bannergress.backend.services.BannerSettingsService;
import com.bannergress.backend.services.PlaceService;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.validation.constraints.Max;

import java.security.Principal;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import static com.bannergress.backend.utils.Spatial.getLatitude;
import static com.bannergress.backend.utils.Spatial.getLongitude;

/**
 * REST endpoint for banners.
 */
@RestController
@Validated
public class BannerController {
    private static final Logger logger = LoggerFactory.getLogger(BannerController.class);

    private final BannerService bannerService;

    private final PlaceService placeService;

    private final BannerSettingsService bannerSettingsService;

    public BannerController(final BannerService bannerService, final PlaceService placeService,
        final BannerSettingsService bannerSettingsService) {
        this.bannerService = bannerService;
        this.placeService = placeService;
        this.bannerSettingsService = bannerSettingsService;
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
                                                @RequestParam final Optional<Collection<BannerListType>> listTypes,
                                                @RequestParam final Optional<BannerSortOrder> orderBy,
                                                @RequestParam(defaultValue = "ASC") final Direction orderDirection,
                                                @RequestParam(defaultValue = "0") final int offset,
                                                @RequestParam(defaultValue = "20") @Max(100) final int limit,
                                                Principal principal) {
        if ((author.isPresent() || listTypes.isPresent()) && principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        int numberOfBounds = (minLatitude.isPresent() ? 1 : 0) + (maxLatitude.isPresent() ? 1 : 0)
            + (minLongitude.isPresent() ? 1 : 0) + (maxLongitude.isPresent() ? 1 : 0);
        if (numberOfBounds != 0 && numberOfBounds != 4) {
            return ResponseEntity.badRequest().build();
        }
        if (orderBy.isPresent() && orderBy.get() == BannerSortOrder.listAdded
            && (!listTypes.isPresent() || listTypes.get().contains(BannerListType.none))) {
            // Sort by list added needs filter by list type(s) other than 'none'
            return ResponseEntity.badRequest().build();
        }
        final Collection<Banner> banners = bannerService.find(placeId, minLatitude, maxLatitude, minLongitude,
            maxLongitude, query, missionId, onlyOfficialMissions, author, listTypes,
            Optional.ofNullable(principal).map(Principal::getName), orderBy, orderDirection, offset, limit);
        List<BannerDto> bannerDtos = banners.stream().map(this::toSummary).collect(Collectors.toUnmodifiableList());
        amendUserSettings(principal, bannerDtos);
        return ResponseEntity.ok(bannerDtos);
    }

    /**
     * Gets a banner with a specified ID.
     *
     * @param id
     * @return
     */
    @GetMapping("/bnrs/{id}")
    public ResponseEntity<BannerDto> get(@PathVariable final String id, Principal principal) {
        final Optional<Banner> banner = bannerService.findBySlugWithDetails(id);
        Optional<BannerDto> optionalBannerDto = banner.map(this::toDetails);
        optionalBannerDto.ifPresent(bannerDto -> amendUserSettings(principal, List.of(bannerDto)));
        return ResponseEntity.of(optionalBannerDto);
    }

    @RolesAllowed(Roles.CREATE_BANNER)
    @PostMapping("/bnrs")
    public ResponseEntity<BannerDto> post(@Valid @RequestBody BannerDto banner, Principal principal)
        throws MissionAlreadyUsedException {
        String id = bannerService.create(banner);
        return get(id, principal);
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
    public ResponseEntity<BannerDto> put(@PathVariable final String id, @Valid @RequestBody BannerDto banner,
                                         Principal principal)
        throws MissionAlreadyUsedException {
        bannerService.update(id, banner);
        return get(id, principal);
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

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/bnrs/{id}/settings")
    public void postSettings(@PathVariable final String id, @Valid @RequestBody BannerSettingsDto settings,
                             Principal principal) {
        bannerSettingsService.addBannerToList(principal.getName(), id, settings.listType);
    }

    private BannerDto toSummary(Banner banner) {
        BannerDto dto = new BannerDto();
        dto.id = banner.getSlug();
        dto.title = banner.getTitle();
        dto.numberOfMissions = banner.getNumberOfMissions();
        dto.lengthMeters = banner.getLengthMeters();
        dto.startLatitude = getLatitude(banner.getStartPoint());
        dto.startLongitude = getLongitude(banner.getStartPoint());
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

    private void amendUserSettings(Principal principal, Collection<BannerDto> bannerDtos) {
        if (principal != null) {
            List<BannerSettings> bannerSettings = bannerSettingsService.getBannerSettings(principal.getName(),
                bannerDtos.stream().map(b -> b.id).collect(Collectors.toList()));
            Map<String, BannerListType> bannerListTypes = bannerSettings.stream()
                .collect(Collectors.toMap(s -> s.getBanner().getSlug(), s -> s.getListType()));
            for (BannerDto bannerDto : bannerDtos) {
                BannerListType listType = bannerListTypes.get(bannerDto.id);
                bannerDto.listType = listType == BannerListType.none ? null : listType;
            }
        }
    }
}
