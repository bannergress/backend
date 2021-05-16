package com.bannergress.backend.services.impl;

import com.bannergress.backend.dto.BannerDto;
import com.bannergress.backend.entities.Banner;
import com.bannergress.backend.entities.Mission;
import com.bannergress.backend.entities.MissionStep;
import com.bannergress.backend.entities.Place;
import com.bannergress.backend.enums.BannerSortOrder;
import com.bannergress.backend.event.BannerChangedEvent;
import com.bannergress.backend.exceptions.MissionAlreadyUsedException;
import com.bannergress.backend.services.BannerPictureService;
import com.bannergress.backend.services.BannerService;
import com.bannergress.backend.services.MissionService;
import com.bannergress.backend.services.PlaceService;
import com.bannergress.backend.utils.SlugGenerator;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Default implementation of {@link BannerService}.
 */
@Service
@Transactional
public class BannerServiceImpl implements BannerService {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private MissionService missionService;

    @Autowired
    private PlaceService placesService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private BannerPictureService pictureService;

    @Autowired
    private SlugGenerator slugGenerator;

    @Override
    public List<Banner> find(Optional<String> placeSlug, Optional<Double> minLatitude, Optional<Double> maxLatitude,
                             Optional<Double> minLongitude, Optional<Double> maxLongitude, Optional<String> search,
                             Optional<BannerSortOrder> orderBy, Direction orderDirection, int offset, int limit) {
        String queryString = "SELECT b FROM Banner b";
        if (placeSlug.isPresent()) {
            queryString += " JOIN b.startPlaces p ";
        }
        queryString += " WHERE true = true";
        if (placeSlug.isPresent()) {
            queryString += " AND p.slug = :placeSlug";
        }
        if (minLatitude.isPresent()) {
            queryString += " AND b.startLatitude BETWEEN :minLatitude AND :maxLatitude "
                + "AND b.startLongitude BETWEEN :minLongitude AND :maxLongitude";
        }
        if (search.isPresent()) {
            queryString += " AND LOWER(b.title) LIKE :search";
        }
        if (orderBy.isPresent()) {
            switch (orderBy.get()) {
                case created:
                    queryString += " ORDER BY b.created " + orderDirection.toString();
                    break;
                case lengthMeters:
                    queryString += " ORDER BY b.lengthMeters " + orderDirection.toString();
                    break;
                case numberOfMissions:
                    queryString += " ORDER BY b.numberOfMissions " + orderDirection.toString();
                    break;
                case title:
                    queryString += " ORDER BY b.title " + orderDirection.toString();
                    break;
            }
        }
        TypedQuery<Banner> query = entityManager.createQuery(queryString, Banner.class);
        if (placeSlug.isPresent()) {
            query.setParameter("placeSlug", placeSlug.get());
        }
        if (minLatitude.isPresent()) {
            query.setParameter("minLatitude", minLatitude.get());
            query.setParameter("maxLatitude", maxLatitude.get());
            query.setParameter("minLongitude", minLongitude.get());
            query.setParameter("maxLongitude", maxLongitude.get());
        }
        if (search.isPresent()) {
            query.setParameter("search", "%" + search.get().toLowerCase() + "%");
        }
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        List<Banner> banners = query.getResultList();
        preloadPlaceInformation(banners);
        return banners;
    }

    private void preloadPlaceInformation(List<Banner> banners) {
        if (!banners.isEmpty()) {
            TypedQuery<Banner> query = entityManager
                .createQuery("SELECT b FROM Banner b LEFT JOIN FETCH b.startPlaces p LEFT JOIN FETCH p.information"
                    + " WHERE b IN :banners", Banner.class);
            query.setParameter("banners", banners);
            query.getResultList();
        }
    }

    @Override
    public Optional<Banner> findBySlugWithDetails(String slug) {
        TypedQuery<Banner> query = entityManager
            .createQuery("SELECT b FROM Banner b LEFT JOIN FETCH b.missions m LEFT JOIN FETCH m.author"
                + " LEFT JOIN FETCH m.steps s LEFT JOIN FETCH s.poi WHERE b.slug = :slug", Banner.class);
        query.setParameter("slug", slug);
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public String create(BannerDto bannerDto) throws MissionAlreadyUsedException {
        Banner banner = createTransient(bannerDto, List.of());
        banner.setSlug(deriveSlug(banner));
        entityManager.persist(banner);
        banner.getStartPlaces().forEach(place -> place.setNumberOfBanners(place.getNumberOfBanners() + 1));
        return banner.getSlug();
    }

    private String deriveSlug(Banner banner) {
        String title = banner.getTitle();
        return slugGenerator.generateSlug(title,
            slug -> entityManager.unwrap(Session.class).bySimpleNaturalId(Banner.class).loadOptional(slug).isEmpty());
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
        banner.getMissions().putAll(Maps.transformValues(bannerDto.missions,
            missionDto -> entityManager.getReference(Mission.class, missionDto.id)));
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
        Banner banner = entityManager.unwrap(Session.class).bySimpleNaturalId(Banner.class).load(slug);
        banner.setTitle(bannerDto.title);
        banner.setDescription(bannerDto.description);
        banner.setWidth(bannerDto.width);
        banner.setType(bannerDto.type);
        banner.getMissions().clear();
        banner.getMissions().putAll(Maps.transformValues(bannerDto.missions,
            missionDto -> entityManager.getReference(Mission.class, missionDto.id)));
        publisher.publishEvent(new BannerChangedEvent(banner));
    }

    @Override
    public void deleteBySlug(String slug) {
        Banner banner = entityManager.unwrap(Session.class).bySimpleNaturalId(Banner.class).load(slug);
        for (Place place : banner.getStartPlaces()) {
            place.setNumberOfBanners(place.getNumberOfBanners() - 1);
        }
        entityManager.remove(banner);
    }

    @Override
    public void calculateData(Banner banner) {
        Double startLatitude = null;
        Double startLongitude = null;
        Double prevLatitude = null;
        Double prevLongitude = null;
        double distance = 0;
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
                    if (latitude != null) {
                        if (prevLatitude != null) {
                            distance += getDistance(latitude, longitude, prevLatitude, prevLongitude);
                        }
                        if (startLatitude == null) {
                            startLatitude = latitude;
                            startLongitude = longitude;
                        }
                        prevLatitude = latitude;
                        prevLongitude = longitude;
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
        banner.setLengthMeters((int) Math.round(distance));
    }

    @Override
    public void calculateAllBanners() {
        TypedQuery<Banner> query = entityManager.createQuery("SELECT b FROM Banner b", Banner.class);
        for (Banner banner : query.getResultList()) {
            publisher.publishEvent(new BannerChangedEvent(banner));
        }
    }

    private static Double getDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        final int radius_meters = 6_371_000;
        Double latDistance = toRad(lat2 - lat1);
        Double lonDistance = toRad(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
            + Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return radius_meters * c;
    }

    private static Double toRad(Double value) {
        return value * Math.PI / 180;
    }
}
