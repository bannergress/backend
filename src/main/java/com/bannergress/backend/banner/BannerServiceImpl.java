package com.bannergress.backend.banner;

import com.bannergress.backend.banner.comment.Comment;
import com.bannergress.backend.banner.comment.RoundTheClockType;
import com.bannergress.backend.banner.picture.BannerPictureService;
import com.bannergress.backend.banner.timezone.TimezoneService;
import com.bannergress.backend.mission.*;
import com.bannergress.backend.mission.step.MissionStep;
import com.bannergress.backend.place.Place;
import com.bannergress.backend.place.PlaceService;
import com.bannergress.backend.spatial.DistanceCalculation;
import com.bannergress.backend.utils.SlugGenerator;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Default implementation of {@link BannerService}.
 */
@Service
@Transactional
public class BannerServiceImpl implements BannerService {
    @Autowired
    private BannerRepository bannerRepository;

    @Autowired
    private MissionRepository missionRepository;

    @Autowired
    private MissionService missionService;

    @Autowired
    private PlaceService placesService;

    @Autowired
    private BannerPictureService pictureService;

    @Autowired
    private TimezoneService timezoneService;

    @Autowired
    private SlugGenerator slugGenerator;

    @Override
    public List<String> findAllSlugs() {
        return bannerRepository.getAllSlugs();
    }

    private void preloadMissions(Collection<Mission> missions) {
        if (!missions.isEmpty()) {
            missionRepository
                .findAll(MissionSpecifications.isInMissions(missions).and(MissionSpecifications.fetchDetails()));
        }
    }

    @Override
    public Optional<Banner> findBySlug(String slug) {
        return bannerRepository.findOne(BannerSpecifications.hasSlug(slug));
    }

    @Override
    public Optional<Banner> findBySlugWithDetails(String slug) {
        return bannerRepository.findAll(BannerSpecifications.hasSlug(slug).and(BannerSpecifications.fetchDetails()))
            .stream().findAny();
    }

    @Override
    public String create(BannerDto bannerDto) throws MissionAlreadyUsedException {
        Banner banner = createTransient(bannerDto, List.of());
        calculateSlug(banner);
        bannerRepository.save(banner);
        banner.getStartPlaces().forEach(p -> p.getBanners().add(banner));
        placesService.updatePlaces(banner.getStartPlaces());
        return banner.getCanonicalSlug();
    }

    private void calculateSlug(Banner banner) {
        // Slug candidates: 1. current canonical slug 2. any previous slug 3. new slug
        Stream<String> slugCandidates = Stream.concat(
            Stream.concat(Optional.ofNullable(banner.getCanonicalSlug()).stream(), banner.getSlugs().stream()),
            Stream.generate(() -> deriveSlug(banner)));
        String slug = slugCandidates.filter(s -> slugGenerator.isDerivedFrom(s, banner.getTitle())).findFirst().get();
        banner.setCanonicalSlug(slug);
        banner.getSlugs().add(slug);
    }

    private String deriveSlug(Banner banner) {
        String title = banner.getTitle();
        return slugGenerator.generateSlug(title,
            slug -> bannerRepository.count(BannerSpecifications.hasSlug(slug)) == 0);
    }

    private Banner createTransient(BannerDto bannerDto, List<String> acceptableBannerSlugs)
        throws MissionAlreadyUsedException {
        Collection<String> missionIds = missionIds(bannerDto);
        missionService.assertNotAlreadyUsedInBanners(missionIds, acceptableBannerSlugs);
        Banner banner = new Banner();
        banner.setTitle(bannerDto.title);
        banner.setDescription(bannerDto.description);
        banner.setCreated(Instant.now());
        banner.setWidth(bannerDto.width);
        banner.setType(bannerDto.type);
        banner.getMissions().clear();
        banner.getMissions().putAll(actualMissionReferences(bannerDto));
        banner.getPlaceholders().clear();
        banner.getPlaceholders().addAll(placeHolderMissions(bannerDto).keySet());
        banner.setWarning(bannerDto.warning);
        banner.setPlannedOfflineDate(bannerDto.plannedOfflineDate);
        banner.setEventStartDate(bannerDto.eventStartDate);
        banner.setEventEndDate(bannerDto.eventEndDate);
        calculateData(banner);
        pictureService.refresh(banner);
        return banner;
    }

    private Map<Integer, Mission> actualMissionReferences(BannerDto bannerDto) {
        return Maps.transformValues(actualMissions(bannerDto), missionDto -> missionRepository.getReferenceById(missionDto.id));
    }

    private static Collection<String> missionIds(BannerDto bannerDto) {
        return Collections2.transform(actualMissions(bannerDto).values(), missionDto -> missionDto.id);
    }

    private static Map<Integer, MissionDto> actualMissions(BannerDto bannerDto) {
        return Maps.filterValues(bannerDto.missions, missionDto -> missionDto.id != null);
    }

    private static Map<Integer, MissionDto> placeHolderMissions(BannerDto bannerDto) {
        return Maps.filterValues(bannerDto.missions, missionDto -> missionDto.id == null);
    }

    @Override
    public Banner generatePreview(BannerDto bannerDto) throws MissionAlreadyUsedException {
        Banner banner = createTransient(bannerDto, bannerDto.id != null ? List.of(bannerDto.id) : List.of());
        pictureService.setPictureExpired(banner.getPicture());
        return banner;
    }

    @Override
    public void update(String slug, BannerDto bannerDto) throws MissionAlreadyUsedException {
        Collection<String> missionIds = missionIds(bannerDto);
        missionService.assertNotAlreadyUsedInBanners(missionIds, List.of(bannerDto.id));
        Banner banner = bannerRepository.findOne(BannerSpecifications.hasSlug(slug)).get();
        banner.setTitle(bannerDto.title);
        banner.setDescription(bannerDto.description);
        banner.setWidth(bannerDto.width);
        banner.setType(bannerDto.type);
        banner.getMissions().clear();
        banner.getMissions().putAll(actualMissionReferences(bannerDto));
        banner.getPlaceholders().clear();
        banner.getPlaceholders().addAll(placeHolderMissions(bannerDto).keySet());
        banner.setWarning(bannerDto.warning);
        banner.setPlannedOfflineDate(bannerDto.plannedOfflineDate);
        banner.setEventStartDate(bannerDto.eventStartDate);
        banner.setEventEndDate(bannerDto.eventEndDate);
        calculateSlug(banner);
        calculateData(banner);
        pictureService.refresh(banner);
    }

    @Override
    public void deleteBySlug(String slug) {
        Banner banner = bannerRepository.findOne(BannerSpecifications.hasSlug(slug)).get();
        pictureService.setPictureExpired(banner.getPicture());
        Set<Place> startPlaces = ImmutableSet.copyOf(banner.getStartPlaces());
        banner.getStartPlaces().forEach(p -> p.getBanners().remove(banner));
        placesService.updatePlaces(startPlaces);
        bannerRepository.delete(banner);
    }

    @Override
    public void calculateData(Banner banner) {
        preloadMissions(banner.getMissions().values());

        Point startPoint = null;
        int numberOfSubmittedMissions = banner.getPlaceholders().size();
        int numberOfDisabledMissions = 0;

        for (Mission mission : banner.getMissions().values()) {
            switch (mission.getStatus()) {
                case disabled:
                    numberOfDisabledMissions++;
                    break;
                case submitted:
                    numberOfSubmittedMissions++;
                    break;
                default:
                    break;
            }
            for (MissionStep step : mission.getSteps()) {
                if (step.getPoi() != null) {
                    Point point = step.getPoi().getPoint();
                    if (startPoint == null) {
                        startPoint = point;
                    }
                }
            }
        }
        banner.setStartPoint(startPoint);
        banner.setOnline(numberOfSubmittedMissions == 0 && numberOfDisabledMissions == 0);
        banner.setNumberOfMissions(banner.getMissions().size() + banner.getPlaceholders().size());
        banner.setNumberOfDisabledMissions(numberOfDisabledMissions);
        banner.setNumberOfSubmittedMissions(numberOfSubmittedMissions);
        if (startPoint != null && banner.getStartPlaces().isEmpty()) {
            Collection<Place> startPlaces = placesService.getPlaces(startPoint);
            banner.getStartPlaces().clear();
            banner.getStartPlaces().addAll(startPlaces);
        }
        if (banner.getEventStartDate() == null && banner.getEventEndDate() == null) {
            banner.setEventStartTimestamp(null);
            banner.setEventEndTimestamp(null);
        } else {
            Preconditions.checkArgument(!banner.getEventStartDate().isAfter(banner.getEventEndDate()));
            ZoneId zone = timezoneService.getZone(startPoint);
            banner.setEventStartTimestamp(banner.getEventStartDate().atStartOfDay(zone).toInstant());
            banner.setEventEndTimestamp(banner.getEventEndDate().plusDays(1).atStartOfDay(zone).toInstant());
        }
        banner.setLengthMeters(DistanceCalculation.calculateLengthMeters(banner.getMissions().values()));
        banner.setAverageRatingRoundTheClock(
            getAverageRating(banner, comment -> comment.getRatingRoundTheClock() == null ? null
                : comment.getRatingRoundTheClock() == RoundTheClockType.unrestricted ? 1 : 0));
        banner.setAverageRatingOverall(getAverageRating(banner, Comment::getRatingOverall));
        banner.setAverageRatingAccessibility(getAverageRating(banner, Comment::getRatingAccessibility));
        banner.setAverageRatingPassphrases(getAverageRating(banner, Comment::getRatingPassphrases));
    }

    private Float getAverageRating(Banner banner, Function<Comment, Integer> ratingFunction) {
        OptionalDouble result = banner.getComments().stream() //
            .map(ratingFunction) //
            .filter(r -> r != null) //
            .mapToInt(r -> r) //
            .average();
        return result.isEmpty() ? null : (float) result.getAsDouble();
    }

    @Override
    public List<UUID> findAllUUIDs() {
        return bannerRepository.getAllUUIDs();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void calculateBanner(UUID uuid) {
        Banner banner = bannerRepository.findById(uuid).get();
        calculateData(banner);
        pictureService.refresh(banner);
    }

    @Override
    public boolean hasAuthor(String slug, String author) {
        return bannerRepository.count(BannerSpecifications.hasSlug(slug)
            .and(BannerSpecifications.hasMissionWith(MissionSpecifications.hasAuthors(List.of(author))))) > 0;
    }

    @Override
    public boolean isProbablyMaliciousEdit(String slug, BannerDto bannerDto, String userId) {
        Banner banner = bannerRepository.findOne(BannerSpecifications.hasSlug(slug)).get();
        Set<Mission> oldMissions = Set.copyOf(banner.getMissions().values());
        Set<Mission> newMissions = Set.copyOf(actualMissionReferences(bannerDto).values());
        // An edit is categorized as probably malicious if the last remaining mission of the editor is removed
        boolean noOwnMissionsRemaining = newMissions.stream()
            .noneMatch(mission -> mission.getAuthor() != null && mission.getAuthor().getName().equals(userId));
        // An edit is categorized as probably malicious if the number of published missions is reduced
        Stream<Mission> oldPublishedMissions = oldMissions.stream()
            .filter(mission -> mission.getStatus() == MissionStatus.published);
        Stream<Mission> newPublishedMissions = newMissions.stream()
            .filter(mission -> mission.getStatus() == MissionStatus.published);
        boolean publishedMissionCountReduced = oldPublishedMissions.count() > newPublishedMissions.count();
        return noOwnMissionsRemaining || publishedMissionCountReduced;
    }

    @Override
    public boolean isMistakeEdit(String slug, BannerDto bannerDto) {
        Banner banner = bannerRepository.findOne(BannerSpecifications.hasSlug(slug)).get();
        Set<Mission> oldMissions = Set.copyOf(banner.getMissions().values());
        Set<Mission> newMissions = Set.copyOf(actualMissionReferences(bannerDto).values());
        return Sets.intersection(oldMissions, newMissions).isEmpty();
    }
}
