package com.bannergress.backend.services.impl;

import com.bannergress.backend.dto.BannerDto;
import com.bannergress.backend.entities.Banner;
import com.bannergress.backend.entities.Mission;
import com.bannergress.backend.entities.MissionStep;
import com.bannergress.backend.entities.Place;
import com.bannergress.backend.enums.BannerListType;
import com.bannergress.backend.enums.BannerSortOrder;
import com.bannergress.backend.enums.MissionStatus;
import com.bannergress.backend.exceptions.MissionAlreadyUsedException;
import com.bannergress.backend.repositories.BannerRepository;
import com.bannergress.backend.repositories.BannerSpecifications;
import com.bannergress.backend.repositories.MissionRepository;
import com.bannergress.backend.repositories.MissionSpecifications;
import com.bannergress.backend.services.BannerPictureService;
import com.bannergress.backend.services.BannerService;
import com.bannergress.backend.services.MissionService;
import com.bannergress.backend.services.PlaceService;
import com.bannergress.backend.utils.DistanceCalculation;
import com.bannergress.backend.utils.OffsetBasedPageRequest;
import com.bannergress.backend.utils.SlugGenerator;
import com.bannergress.backend.utils.Spatial;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

/**
 * Default implementation of {@link BannerService}.
 */
@Service
@Transactional
public class BannerServiceImpl implements BannerService {
    private static final List<String> OFFICIAL_MISSION_AUTHORS = ImmutableList.of( //
        "MissionByNia", //
        "MissionsByNIA", //
        "MissionDaysNia", //
        "MissionsNIA", //
        "MDNia2", //
        "MDNIA", //
        "MDNIA2020" //
    );

    @Autowired
    private BannerRepository bannerRepository;

    @Autowired
    private MissionRepository missionRepository;

    @Autowired
    private MissionService missionService;

    @Autowired
    private PlaceService placesService;

    @Autowired
    private BannerService bannerService;

    @Autowired
    private BannerPictureService pictureService;

    @Autowired
    private SlugGenerator slugGenerator;

    @Autowired
    private Spatial spatial;

    @Override
    public List<Banner> find(Optional<String> placeSlug, Optional<Double> minLatitude, Optional<Double> maxLatitude,
                             Optional<Double> minLongitude, Optional<Double> maxLongitude, Optional<String> search,
                             Optional<String> missionId, boolean onlyOfficialMissions, Optional<String> author,
                             Optional<Collection<BannerListType>> listTypes, Optional<String> userId,
                             Optional<BannerSortOrder> orderBy, Direction orderDirection,
                             Optional<Double> proximityLatitude, Optional<Double> proximityLongitude, int offset,
                             int limit) {
        List<Specification<Banner>> specifications = new ArrayList<>();
        if (placeSlug.isPresent()) {
            specifications.add(BannerSpecifications.hasStartPlaceSlug(placeSlug.get()));
        }
        if (minLatitude.isPresent()) {
            if (minLongitude.get() <= maxLongitude.get()) {
                Geometry box = spatial.createBoundingBox(minLatitude.get(), maxLatitude.get(), minLongitude.get(),
                    maxLongitude.get());
                specifications.add(BannerSpecifications.startPointIntersects(box));
            } else {
                Geometry box1 = spatial.createBoundingBox(minLatitude.get(), maxLatitude.get(), maxLongitude.get(),
                    180);
                Geometry box2 = spatial.createBoundingBox(minLatitude.get(), maxLatitude.get(), -180,
                    minLongitude.get());
                specifications.add(BannerSpecifications.startPointIntersects(box1)
                    .or(BannerSpecifications.startPointIntersects(box2)));
            }
        }
        if (search.isPresent()) {
            specifications.add(BannerSpecifications.hasTitlePart(search.get()));
        }
        if (missionId.isPresent()) {
            specifications.add(BannerSpecifications.hasMissionId(missionId.get()));
        }
        if (onlyOfficialMissions) {
            specifications
                .add(BannerSpecifications.hasMissionWith(MissionSpecifications.hasAuthors(OFFICIAL_MISSION_AUTHORS)));
        }
        if (author.isPresent()) {
            specifications
                .add(BannerSpecifications.hasMissionWith(MissionSpecifications.hasAuthors(List.of(author.get()))));
        }
        if (listTypes.isPresent()) {
            if (orderBy.isPresent() && orderBy.get() == BannerSortOrder.listAdded) {
                specifications
                    .add(BannerSpecifications.isInUserListSorted(listTypes.get(), userId.get(), orderDirection));
            } else {
                specifications.add(BannerSpecifications.isInUserList(listTypes.get(), userId.get()));
            }
        }

        Sort sort;
        if (orderBy.isPresent()) {
            switch (orderBy.get()) {
                case listAdded:
                    sort = Sort.unsorted(); // Sorting takes place in the specification
                    break;
                case proximityStartPoint:
                    specifications.add(BannerSpecifications.sortByProximity(
                        spatial.createPoint(proximityLatitude.get(), proximityLongitude.get()), orderDirection));
                    sort = Sort.unsorted(); // Sorting takes place in the specification
                    break;
                default:
                    sort = Sort.by(orderDirection, orderBy.get().toString(), "uuid");
                    break;
            }
        } else {
            sort = Sort.by(Direction.ASC, "uuid");
        }

        Specification<Banner> fullSpecification = specifications.stream().reduce((a, b) -> a.and(b)).orElse(null);

        OffsetBasedPageRequest request = new OffsetBasedPageRequest(offset, limit, sort);
        List<Banner> banners = bannerRepository.findAll(fullSpecification, request).getContent();
        preloadPlaceInformation(banners);
        return banners;
    }

    @Override
    public List<String> findAllSlugs() {
        return bannerRepository.getAllSlugs();
    }

    private void preloadPlaceInformation(List<Banner> banners) {
        if (!banners.isEmpty()) {
            bannerRepository
                .findAll(BannerSpecifications.isInBanners(banners).and(BannerSpecifications.fetchPlaceInformation()));
        }
    }

    @Override
    public Optional<Banner> findBySlugWithDetails(String slug) {
        return bannerRepository.findOne(BannerSpecifications.hasSlug(slug).and(BannerSpecifications.fetchDetails()));
    }

    @Override
    public String create(BannerDto bannerDto) throws MissionAlreadyUsedException {
        Banner banner = createTransient(bannerDto, List.of());
        calculateSlug(banner);
        bannerRepository.save(banner);
        banner.getStartPlaces().forEach(place -> place.setNumberOfBanners(place.getNumberOfBanners() + 1));
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
        Collection<String> missionIds = Collections2.transform(bannerDto.missions.values(),
            missionDto -> missionDto.id);
        missionService.assertNotAlreadyUsedInBanners(missionIds, acceptableBannerSlugs);
        Banner banner = new Banner();
        banner.setTitle(bannerDto.title);
        banner.setDescription(bannerDto.description);
        banner.setCreated(Instant.now());
        banner.setWidth(bannerDto.width);
        banner.setType(bannerDto.type);
        banner.getMissions().clear();
        banner.getMissions()
            .putAll(Maps.transformValues(bannerDto.missions, missionDto -> missionRepository.getOne(missionDto.id)));
        calculateData(banner);
        pictureService.refresh(banner);
        return banner;
    }

    @Override
    public Banner generatePreview(BannerDto bannerDto) throws MissionAlreadyUsedException {
        Banner banner = createTransient(bannerDto, bannerDto.id != null ? List.of(bannerDto.id) : List.of());
        banner.getPicture().setExpiration(Instant.now().plusSeconds(3_600));
        return banner;
    }

    @Override
    public void update(String slug, BannerDto bannerDto) throws MissionAlreadyUsedException {
        Collection<String> missionIds = Collections2.transform(bannerDto.missions.values(),
            missionDto -> missionDto.id);
        missionService.assertNotAlreadyUsedInBanners(missionIds, List.of(bannerDto.id));
        Banner banner = bannerRepository.findOne(BannerSpecifications.hasSlug(slug)).get();
        banner.setTitle(bannerDto.title);
        banner.setDescription(bannerDto.description);
        banner.setWidth(bannerDto.width);
        banner.setType(bannerDto.type);
        banner.getMissions().clear();
        banner.getMissions()
            .putAll(Maps.transformValues(bannerDto.missions, missionDto -> missionRepository.getOne(missionDto.id)));
        calculateSlug(banner);
        bannerService.calculateData(banner);
        pictureService.refresh(banner);
    }

    @Override
    public void deleteBySlug(String slug) {
        Banner banner = bannerRepository.findOne(BannerSpecifications.hasSlug(slug)).get();
        for (Place place : banner.getStartPlaces()) {
            place.setNumberOfBanners(place.getNumberOfBanners() - 1);
        }
        bannerRepository.delete(banner);
    }

    @Override
    public void calculateData(Banner banner) {
        Point startPoint = null;
        boolean complete = true;
        boolean online = complete;

        for (Mission mission : banner.getMissions().values()) {
            online &= mission.getStatus() == MissionStatus.published;
            if (mission.getType() == null) {
                complete = false;
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
        banner.setComplete(complete);
        banner.setOnline(online);
        banner.setNumberOfMissions(banner.getMissions().size());
        if (startPoint != null && banner.getStartPlaces().isEmpty()) {
            Collection<Place> startPlaces = placesService.getPlaces(startPoint);
            banner.getStartPlaces().clear();
            banner.getStartPlaces().addAll(startPlaces);
        }
        banner.setLengthMeters(DistanceCalculation.calculateLengthMeters(banner.getMissions().values()));
    }

    @Override
    public List<UUID> findAllUUIDs() {
        return bannerRepository.getAllUUIDs();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void calculateBanner(UUID uuid) {
        Banner banner = bannerRepository.findById(uuid).get();
        bannerService.calculateData(banner);
        pictureService.refresh(banner);
    }
}
