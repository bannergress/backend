package com.bannergress.backend.services.impl;

import com.bannergress.backend.dto.BannerDto;
import com.bannergress.backend.entities.Banner;
import com.bannergress.backend.entities.Mission;
import com.bannergress.backend.entities.MissionStep;
import com.bannergress.backend.entities.Place;
import com.bannergress.backend.enums.BannerSortOrder;
import com.bannergress.backend.exceptions.MissionAlreadyUsedException;
import com.bannergress.backend.repositories.BannerRepository;
import com.bannergress.backend.repositories.BannerSpecifications;
import com.bannergress.backend.repositories.MissionRepository;
import com.bannergress.backend.services.BannerPictureService;
import com.bannergress.backend.services.BannerService;
import com.bannergress.backend.services.MissionService;
import com.bannergress.backend.services.PlaceService;
import com.bannergress.backend.utils.DistanceCalculation;
import com.bannergress.backend.utils.SlugGenerator;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

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
    private BannerService bannerService;

    @Autowired
    private BannerPictureService pictureService;

    @Autowired
    private SlugGenerator slugGenerator;

    @Override
    public List<Banner> find(Optional<String> placeSlug, Optional<Double> minLatitude, Optional<Double> maxLatitude,
                             Optional<Double> minLongitude, Optional<Double> maxLongitude, Optional<String> search,
                             Optional<String> missionId, Optional<BannerSortOrder> orderBy, Direction orderDirection,
                             int offset, int limit) {
        List<Specification<Banner>> specifications = new ArrayList<>();
        if (placeSlug.isPresent()) {
            specifications.add(BannerSpecifications.hasStartPlaceSlug(placeSlug.get()));
        }
        if (minLatitude.isPresent()) {
            specifications.add(BannerSpecifications.isInLatitudeRange(minLatitude.get(), maxLatitude.get()));
            if (minLongitude.get() <= maxLongitude.get()) {
                specifications.add(BannerSpecifications.isInLongitudeRange(minLongitude.get(), maxLongitude.get()));
            } else {
                specifications.add(BannerSpecifications.isInLongitudeRange(minLongitude.get(), 180)
                    .or(BannerSpecifications.isInLongitudeRange(-180, maxLongitude.get())));
            }
        }
        if (search.isPresent()) {
            specifications.add(BannerSpecifications.hasTitlePart(search.get()));
        }
        if (missionId.isPresent()) {
            specifications.add(BannerSpecifications.hasMissionId(missionId.get()));
        }

        Specification<Banner> fullSpecification = specifications.stream().reduce((a, b) -> a.and(b)).orElse(null);
        Sort sort = orderBy.isPresent() ? Sort.by(orderDirection, orderBy.get().toString()) : Sort.unsorted();
        List<Banner> banners = bannerRepository.findAll(fullSpecification, sort);
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
        banner.setSlug(deriveSlug(banner));
        bannerRepository.save(banner);
        banner.getStartPlaces().forEach(place -> place.setNumberOfBanners(place.getNumberOfBanners() + 1));
        return banner.getSlug();
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
        Double startLatitude = null;
        Double startLongitude = null;
        boolean complete = true;
        boolean online = complete;

        for (Mission mission : banner.getMissions().values()) {
            online &= mission.isOnline();
            if (mission.getType() == null) {
                complete = false;
            }
            for (MissionStep step : mission.getSteps()) {
                if (step.getPoi() != null) {
                    Double latitude = step.getPoi().getLatitude();
                    Double longitude = step.getPoi().getLongitude();
                    if (latitude != null && startLatitude == null) {
                        startLatitude = latitude;
                        startLongitude = longitude;
                    }
                }
            }
        }
        banner.setStartLatitude(startLatitude);
        banner.setStartLongitude(startLongitude);
        banner.setComplete(complete);
        banner.setOnline(online);
        banner.setNumberOfMissions(banner.getMissions().size());
        if (startLatitude != null && banner.getStartPlaces().isEmpty()) {
            Collection<Place> startPlaces = placesService.getPlaces(startLatitude, startLongitude);
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
