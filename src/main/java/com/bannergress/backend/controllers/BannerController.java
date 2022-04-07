package com.bannergress.backend.controllers;

import com.bannergress.backend.dto.BannerDto;
import com.bannergress.backend.dto.BannerSettingsDto;
import com.bannergress.backend.dto.MissionDto;
import com.bannergress.backend.entities.Banner;
import com.bannergress.backend.entities.BannerSettings;
import com.bannergress.backend.entities.Mission;
import com.bannergress.backend.entities.PlaceInformation;
import com.bannergress.backend.enums.BannerListType;
import com.bannergress.backend.enums.BannerSortOrder;
import com.bannergress.backend.exceptions.MissionAlreadyUsedException;
import com.bannergress.backend.security.Roles;
import com.bannergress.backend.services.BannerSearchService;
import com.bannergress.backend.services.BannerService;
import com.bannergress.backend.services.BannerSettingsService;
import com.bannergress.backend.services.PlaceService;
import com.bannergress.backend.validation.NianticId;
import com.google.common.collect.Maps;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

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
@RestController("bannerController")
@Validated
public class BannerController {
    private static final String AGENT_TOKEN_ATTRIBUTE = "agent";

    private static final Logger logger = LoggerFactory.getLogger(BannerController.class);

    private final BannerService bannerService;

    private final BannerSearchService bannerSearchService;

    private final PlaceService placeService;

    private final BannerSettingsService bannerSettingsService;

    public BannerController(final BannerService bannerService, final PlaceService placeService,
        final BannerSettingsService bannerSettingsService, BannerSearchService bannerSearchService) {
        this.bannerService = bannerService;
        this.placeService = placeService;
        this.bannerSettingsService = bannerSettingsService;
        this.bannerSearchService = bannerSearchService;
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
    public ResponseEntity<List<BannerDto>> list(@RequestParam @Parameter(description = "Place ID the banner belongs to.") final Optional<String> placeId,
                                                @RequestParam @Parameter(description = "Minimum latitude of the banner start point bounding box. Only valid in combination with all other bounding box parameters.") final Optional<@Min(-90) @Max(90) Double> minLatitude,
                                                @RequestParam @Parameter(description = "Maximum latitude of the banner start point bounding box. Only valid in combination with all other bounding box parameters.") final Optional<@Min(-90) @Max(90) Double> maxLatitude,
                                                @RequestParam @Parameter(description = "Minimum longitude of the banner start point bounding box. Only valid in combination with all other bounding box parameters.") final Optional<@Min(-180) @Max(180) Double> minLongitude,
                                                @RequestParam @Parameter(description = "Minimum longitude of the banner start point bounding box. Only valid in combination with all other bounding box parameters.") final Optional<@Min(-180) @Max(180) Double> maxLongitude,
                                                @RequestParam @Parameter(description = "Query string. The exact search algorithm may change over time.") final Optional<String> query,
                                                @RequestParam @Parameter(description = "ID of a mission that is part of the banner.") final Optional<@NianticId String> missionId,
                                                @RequestParam(defaultValue = "false") @Parameter(description = "Only banners with missions created by Niantic.") final boolean onlyOfficialMissions,
                                                @RequestParam @Parameter(description = "Agent who created one of the missions of the banner.") final Optional<String> author,
                                                @RequestParam @Parameter(description = "List(s) the banner is on (requires authentication).") final Optional<Collection<BannerListType>> listTypes,
                                                @RequestParam @Parameter(description = "Only list online/offline banners.") final Optional<Boolean> online,
                                                @RequestParam @Parameter(description = "Sort order.") final Optional<BannerSortOrder> orderBy,
                                                @RequestParam(defaultValue = "ASC") @Parameter(description = "Order direction.") final Direction orderDirection,
                                                @RequestParam @Parameter(description = "Latitude of the proximity reference point. Required for orderBy=proximityStartPoint.") final Optional<@Min(-90) @Max(90) Double> proximityLatitude,
                                                @RequestParam @Parameter(description = "Longitude of the proximity reference point. Required for orderBy=proximityStartPoint.") final Optional<@Min(-180) @Max(180) Double> proximityLongitude,
                                                @RequestParam(defaultValue = "0") @Parameter(description = "0-based offset for searching.") @Min(0) final int offset,
                                                @RequestParam(defaultValue = "20") @Parameter(description = "Maximum number of results.") @Min(1) @Max(100) final int limit,
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
        if (orderBy.isPresent() && orderBy.get() == BannerSortOrder.proximityStartPoint
            && (proximityLatitude.isEmpty() || proximityLongitude.isEmpty())) {
            // Sort by proximity to start point needs reference coordinates
            return ResponseEntity.badRequest().build();
        }
        final Collection<Banner> banners = bannerSearchService.find(placeId, minLatitude, maxLatitude, minLongitude,
            maxLongitude, query, missionId, onlyOfficialMissions, author, listTypes,
            Optional.ofNullable(principal).map(Principal::getName), online, orderBy, orderDirection, proximityLatitude,
            proximityLongitude, offset, limit);
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
        optionalBannerDto.ifPresent(bannerDto -> {
            amendUserSettings(principal, List.of(bannerDto));
            amendOwner(principal, bannerDto);
        });
        return ResponseEntity.of(optionalBannerDto);
    }

    @RolesAllowed(Roles.CREATE_BANNER)
    @PostMapping("/bnrs")
    @Hidden
    public ResponseEntity<BannerDto> post(@Valid @RequestBody BannerDto banner, Principal principal)
        throws MissionAlreadyUsedException {
        String id = bannerService.create(banner);
        return get(id, principal);
    }

    @RolesAllowed(Roles.CREATE_BANNER)
    @PostMapping("/bnrs/preview")
    @Hidden
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
    @RolesAllowed(Roles.CREATE_BANNER)
    @PreAuthorize("hasRole('" + Roles.MANAGE_BANNERS + "') or @bannerController.hasOwner(#id, #principal)")
    @PutMapping("/bnrs/{id}")
    @Hidden
    public ResponseEntity<BannerDto> put(@PathVariable final String id, @Valid @RequestBody BannerDto banner,
                                         Principal principal, HttpServletRequest request)
        throws MissionAlreadyUsedException {
        if (bannerService.isMistakeEdit(id, banner)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } else if (!request.isUserInRole(Roles.MANAGE_BANNERS)
            && bannerService.isProbablyMaliciousEdit(id, banner, getAgent(principal).get())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        bannerService.update(id, banner);
        return get(id, principal);
    }

    public boolean hasOwner(String id, Principal principal) {
        return getAgent(principal).map(agent -> bannerService.hasAuthor(id, agent)).orElse(false);
    }

    private Optional<String> getAgent(Principal principal) {
        if (principal instanceof KeycloakAuthenticationToken) {
            return getAgent((Principal) ((KeycloakAuthenticationToken) principal).getPrincipal());
        } else if (principal instanceof KeycloakPrincipal) {
            return Optional.ofNullable((String) ((KeycloakPrincipal<?>) principal).getKeycloakSecurityContext()
                .getToken().getOtherClaims().get(AGENT_TOKEN_ATTRIBUTE));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Deletes the banner with the specified UUID.
     *
     * @param uuid UUID.
     */
    @RolesAllowed(Roles.MANAGE_BANNERS)
    @DeleteMapping("/bnrs/{id}")
    @Hidden
    public void delete(@PathVariable final String id) {
        bannerService.deleteBySlug(id);
    }

    @RolesAllowed(Roles.MANAGE_BANNERS)
    @PostMapping("/bnrs/recalculate")
    @Hidden
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

    @RolesAllowed(Roles.MANAGE_BANNERS)
    @PostMapping("/bnrs/reindex")
    @Hidden
    public void reindexAllBanners() {
        bannerSearchService.updateIndex();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/bnrs/{id}/settings")
    @Hidden
    public void postSettings(@PathVariable final String id, @Valid @RequestBody BannerSettingsDto settings,
                             Principal principal) {
        bannerSettingsService.addBannerToList(principal.getName(), id, settings.listType);
    }

    private BannerDto toSummary(Banner banner) {
        BannerDto dto = new BannerDto();
        dto.id = banner.getCanonicalSlug();
        dto.title = banner.getTitle();
        dto.numberOfMissions = banner.getNumberOfMissions();
        dto.numberOfSubmittedMissions = banner.getNumberOfSubmittedMissions();
        dto.numberOfDisabledMissions = banner.getNumberOfDisabledMissions();
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
        dto.missions = Maps.transformValues(banner.getMissionsAndPlaceholders(), this::toMissionOrPlaceholder);
        dto.type = banner.getType();
        dto.description = banner.getDescription();
        return dto;
    }

    private MissionDto toMissionOrPlaceholder(Optional<Mission> input) {
        return input.map(MissionController::toDetails).orElse(new MissionDto());
    }

    private void amendUserSettings(Principal principal, Collection<BannerDto> bannerDtos) {
        if (principal != null) {
            List<BannerSettings> bannerSettings = bannerSettingsService.getBannerSettings(principal.getName(),
                bannerDtos.stream().map(b -> b.id).collect(Collectors.toList()));
            Map<String, BannerListType> bannerListTypes = bannerSettings.stream()
                .collect(Collectors.toMap(s -> s.getBanner().getCanonicalSlug(), s -> s.getListType()));
            for (BannerDto bannerDto : bannerDtos) {
                BannerListType listType = bannerListTypes.get(bannerDto.id);
                bannerDto.listType = listType == BannerListType.none ? null : listType;
            }
        }
    }

    private void amendOwner(Principal principal, BannerDto bannerDto) {
        if (principal != null) {
            bannerDto.owner = hasOwner(bannerDto.id, principal);
        }
    }
}
