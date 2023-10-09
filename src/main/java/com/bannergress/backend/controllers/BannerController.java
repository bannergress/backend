package com.bannergress.backend.controllers;

import com.bannergress.backend.dto.BannerDto;
import com.bannergress.backend.dto.BannerSettingsDto;
import com.bannergress.backend.dto.Gpx;
import com.bannergress.backend.dto.MissionDto;
import com.bannergress.backend.entities.*;
import com.bannergress.backend.enums.BannerDtoAttribute;
import com.bannergress.backend.enums.BannerListType;
import com.bannergress.backend.enums.BannerSortOrder;
import com.bannergress.backend.enums.POIType;
import com.bannergress.backend.exceptions.MissionAlreadyUsedException;
import com.bannergress.backend.security.Roles;
import com.bannergress.backend.services.BannerSearchService;
import com.bannergress.backend.services.BannerService;
import com.bannergress.backend.services.BannerSettingsService;
import com.bannergress.backend.services.PlaceService;
import com.bannergress.backend.validation.NianticId;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    /** Default set of attributes to return for list queries. */
    private static final Set<BannerDtoAttribute> DEFAULT_LIST_ATTRIBUTES = ImmutableSet.of( //
        BannerDtoAttribute.id, //
        BannerDtoAttribute.title, //
        BannerDtoAttribute.numberOfMissions, //
        BannerDtoAttribute.numberOfSubmittedMissions, //
        BannerDtoAttribute.numberOfDisabledMissions, //
        BannerDtoAttribute.lengthMeters, //
        BannerDtoAttribute.startLatitude, //
        BannerDtoAttribute.startLongitude, //
        BannerDtoAttribute.picture, //
        BannerDtoAttribute.width, //
        BannerDtoAttribute.startPlaceId, //
        BannerDtoAttribute.formattedAddress, //
        BannerDtoAttribute.listType //
    );

    /** Set of attributes to return for preview queries. */
    private static final Set<BannerDtoAttribute> DEFAULT_PREVIEW_ATTRIBUTES = ImmutableSet.<BannerDtoAttribute>builder() //
        .addAll(DEFAULT_LIST_ATTRIBUTES) //
        .add(BannerDtoAttribute.missions) //
        .add(BannerDtoAttribute.type) //
        .add(BannerDtoAttribute.description) //
        .add(BannerDtoAttribute.warning) //
        .add(BannerDtoAttribute.plannedOfflineDate) //
        .add(BannerDtoAttribute.eventStartDate) //
        .add(BannerDtoAttribute.eventEndDate) //
        .build();

    /** Default set of attributes to return for single result queries. */
    private static final Set<BannerDtoAttribute> DEFAULT_GET_ATTRIBUTES = ImmutableSet.<BannerDtoAttribute>builder() //
        .addAll(DEFAULT_PREVIEW_ATTRIBUTES) //
        .add(BannerDtoAttribute.owner) //
        .build();

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
                                                @RequestParam @Parameter(description = "Only list events which end after this ISO 8601 UTC timestamp.") Optional<Instant> minEventTimestamp,
                                                @RequestParam @Parameter(description = "Only list events which start before this ISO 8601 UTC timestamp.") Optional<Instant> maxEventTimestamp,
                                                @RequestParam @Parameter(description = "Include these attributes in the output.") Optional<Set<BannerDtoAttribute>> attributes,
                                                @RequestParam(defaultValue = "0") @Parameter(description = "0-based offset for searching.") @Min(0) final int offset,
                                                @RequestParam(defaultValue = "20") @Parameter(description = "Maximum number of results.") @Min(1) @Max(100) final int limit,
                                                Principal principal, List<Locale.LanguageRange> languagePriorityList) {
        boolean isAuthenticated = principal != null;
        if ((author.isPresent() || listTypes.isPresent()) && !isAuthenticated) {
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
            maxLongitude, query, isAuthenticated, missionId, onlyOfficialMissions, author, listTypes,
            Optional.ofNullable(principal).map(Principal::getName), online, orderBy, orderDirection, proximityLatitude,
            proximityLongitude, minEventTimestamp, maxEventTimestamp, offset, limit);
        List<BannerDto> bannerDtos = banners.stream()
            .map(banner -> toDto(banner, languagePriorityList, attributes.orElse(DEFAULT_LIST_ATTRIBUTES), principal, getListTypeFunction(principal, banners)))
            .collect(Collectors.toUnmodifiableList());
        return ResponseEntity.ok(bannerDtos);
    }

    /**
     * Gets a banner with a specified ID.
     *
     * @param id
     * @return
     */
    @GetMapping("/bnrs/{id}")
    public ResponseEntity<BannerDto> get(@PathVariable final String id,
                                         @RequestParam Optional<Set<BannerDtoAttribute>> attributes,
                                         Principal principal,
                                         List<Locale.LanguageRange> languagePriorityList) {
        final Optional<Banner> banner = bannerService.findBySlugWithDetails(id);
        Optional<BannerDto> optionalBannerDto = banner
            .map(b -> toDto(b, languagePriorityList, attributes.orElse(DEFAULT_GET_ATTRIBUTES), principal,
                getListTypeFunction(principal, ImmutableList.of(b))));
        return ResponseEntity.of(optionalBannerDto);
    }

    /**
     * Gets a banner with a specified ID as GPX file.
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/bnrs/{id}/gpx", produces = Gpx.MIMETYPE)
    public ResponseEntity<Gpx> getGpx(@PathVariable final String id) {
        return bannerService.findBySlugWithDetails(id) //
            .map(banner -> {
                Gpx gpx = toGpx(banner);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentDisposition(ContentDisposition.attachment()
                    .filename(banner.getCanonicalSlug() + Gpx.SUFFIX, StandardCharsets.UTF_8).build());
                return ResponseEntity.ok().headers(headers).body(gpx);
            }) //
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @RolesAllowed(Roles.CREATE_BANNER)
    @PostMapping("/bnrs")
    @Hidden
    public ResponseEntity<BannerDto> post(@Valid @RequestBody BannerDto banner, Principal principal,
                                          List<Locale.LanguageRange> languagePriorityList)
        throws MissionAlreadyUsedException {
        String id = bannerService.create(banner);
        return get(id, Optional.of(DEFAULT_GET_ATTRIBUTES), principal, languagePriorityList);
    }

    @RolesAllowed(Roles.CREATE_BANNER)
    @PostMapping("/bnrs/preview")
    @Hidden
    public BannerDto preview(@Valid @RequestBody BannerDto banner, List<Locale.LanguageRange> languagePriorityList)
        throws MissionAlreadyUsedException {
        return toDto(bannerService.generatePreview(banner), languagePriorityList, DEFAULT_PREVIEW_ATTRIBUTES, null,
            x -> Optional.empty());
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
                                         Principal principal, HttpServletRequest request,
                                         List<Locale.LanguageRange> languagePriorityList)
        throws MissionAlreadyUsedException {
        if (bannerService.isMistakeEdit(id, banner)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } else if (!request.isUserInRole(Roles.MANAGE_BANNERS)
            && (bannerService.isProbablyMaliciousEdit(id, banner, getAgent(principal).get())
                || banner.eventStartDate != null || banner.eventEndDate != null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        bannerService.update(id, banner);
        return get(id, Optional.of(DEFAULT_GET_ATTRIBUTES), principal, languagePriorityList);
    }

    public boolean hasOwner(String id, Principal principal) {
        return getAgent(principal).map(agent -> bannerService.hasAuthor(id, agent)).orElse(false);
    }

    private Optional<String> getAgent(Principal principal) {
        if (principal instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken token = (JwtAuthenticationToken) principal;
            return Optional.ofNullable((String) token.getTokenAttributes().get(AGENT_TOKEN_ATTRIBUTE));
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

    private BannerDto toDto(Banner banner, List<Locale.LanguageRange> languagePriorityList,
                            Collection<BannerDtoAttribute> attributes, Principal principal,
                            Function<String, Optional<BannerListType>> listTypeFunction) {
        Supplier<Optional<PlaceInformation>> placeInformationSupplier = Suppliers
            .memoize(() -> placeService.getMostAccuratePlaceInformation(banner.getStartPlaces(), languagePriorityList));
        BannerDto dto = new BannerDto();
        for (BannerDtoAttribute attribute : attributes) {
            switch (attribute) {
                case description:
                    dto.description = banner.getDescription();
                    break;
                case eventEndDate:
                    dto.eventEndDate = banner.getEventEndDate();
                    break;
                case eventStartDate:
                    dto.eventStartDate = banner.getEventStartDate();
                    break;
                case formattedAddress:
                    dto.formattedAddress = placeInformationSupplier.get().map(PlaceInformation::getFormattedAddress)
                        .orElse(null);
                    break;
                case id:
                    dto.id = banner.getCanonicalSlug();
                    break;
                case lengthMeters:
                    dto.lengthMeters = banner.getLengthMeters();
                    break;
                case listType:
                    BannerListType listType = listTypeFunction.apply(banner.getCanonicalSlug()).orElse(BannerListType.none);
                    dto.listType = listType == BannerListType.none ? null : listType;
                    break;
                case missions:
                    dto.missions = Maps.transformValues(banner.getMissionsAndPlaceholders(), this::toMissionOrPlaceholder);
                    break;
                case numberOfDisabledMissions:
                    dto.numberOfDisabledMissions = banner.getNumberOfDisabledMissions();
                    break;
                case numberOfMissions:
                    dto.numberOfMissions = banner.getNumberOfMissions();
                    break;
                case numberOfSubmittedMissions:
                    dto.numberOfSubmittedMissions = banner.getNumberOfSubmittedMissions();
                    break;
                case owner:
                    dto.owner = hasOwner(dto.id, principal);
                    break;
                case picture:
                    dto.picture = banner.getPicture() == null ? null
                        : ("/bnrs/pictures/" + banner.getPicture().getHash());
                    break;
                case plannedOfflineDate:
                    dto.plannedOfflineDate = banner.getPlannedOfflineDate();
                    break;
                case startLatitude:
                    dto.startLatitude = getLatitude(banner.getStartPoint());
                    break;
                case startLongitude:
                    dto.startLongitude = getLongitude(banner.getStartPoint());
                    break;
                case startPlaceId:
                    dto.startPlaceId = placeInformationSupplier.get().map(p -> p.getPlace().getSlug()).orElse(null);
                    break;
                case title:
                    dto.title = banner.getTitle();
                    break;
                case type:
                    dto.type = banner.getType();
                    break;
                case warning:
                    dto.warning = banner.getWarning();
                    break;
                case width:
                    dto.width = banner.getWidth();
                    break;
                default:
                    throw new IllegalArgumentException(attribute.toString());
            }
        }
        return dto;
    }

    private MissionDto toMissionOrPlaceholder(Optional<Mission> input) {
        return input.map(MissionController::toDetails).orElse(new MissionDto());
    }

    private Function<String, Optional<BannerListType>> getListTypeFunction(Principal principal,
                                                                           Collection<Banner> banners) {
        if (principal == null) {
            return canonicalSlug -> Optional.empty();
        } else {
            Supplier<List<BannerSettings>> bannerSettingsSupplier = () -> bannerSettingsService.getBannerSettings(
                principal.getName(), banners.stream().map(Banner::getCanonicalSlug).collect(Collectors.toList()));
            return canonicalSlug -> bannerSettingsSupplier.get().stream()
                .filter(settings -> settings.getBanner().getCanonicalSlug().equals(canonicalSlug)).findFirst()
                .map(BannerSettings::getListType);
        }
    }

    private Gpx toGpx(Banner banner) {
        Gpx result = new Gpx();
        result.metadata = new Gpx.Metadata();
        result.metadata.name = banner.getTitle();
        result.rte = new Gpx.Route();
        result.rte.name = banner.getTitle();
        result.rte.rtept = streamPoisWithoutConsecutiveDuplicates(banner) //
            .map(poi -> {
                Gpx.Waypoint waypoint = new Gpx.Waypoint();
                waypoint.name = poi.getTitle();
                waypoint.lat = getLatitude(poi.getPoint());
                waypoint.lon = getLongitude(poi.getPoint());
                return waypoint;
            }) //
            .collect(Collectors.toList());
        return result;
    }

    private Stream<POI> streamPoisWithoutConsecutiveDuplicates(Banner banner) {
        return streamPois(banner) //
            .sequential() // Stateful filter requires sequential stream
            .filter(new Predicate<POI>() {
                private String previousId;

                @Override
                public boolean test(POI poi) {
                    if (!poi.getId().equals(previousId)) {
                        previousId = poi.getId();
                        return true;
                    } else {
                        return false;
                    }
                }
            });
    }

    private Stream<POI> streamPois(Banner banner) {
        return banner.getMissions().values().stream() //
            .flatMap(mission -> mission.getSteps().stream()) //
            .filter(step -> step.getPoi() != null) //
            .map(MissionStep::getPoi) //
            .filter(poi -> poi.getType() != POIType.unavailable);
    }
}
