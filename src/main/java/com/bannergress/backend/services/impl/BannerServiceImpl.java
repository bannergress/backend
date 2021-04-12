package com.bannergress.backend.services.impl;

import com.bannergress.backend.dto.BannerDto;
import com.bannergress.backend.entities.Banner;
import com.bannergress.backend.entities.Mission;
import com.bannergress.backend.entities.MissionStep;
import com.bannergress.backend.entities.Place;
import com.bannergress.backend.enums.BannerSortOrder;
import com.bannergress.backend.services.BannerPictureService;
import com.bannergress.backend.services.BannerService;
import com.bannergress.backend.services.GeocodingService;
import com.bannergress.backend.services.MissionService;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Default implementation of {@link BannerService}.
 */
@Service
@Transactional(isolation = Isolation.SERIALIZABLE)
public class BannerServiceImpl implements BannerService {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private MissionService missionService;

    @Autowired
    private GeocodingService geocodingService;

    @Autowired
    private BannerPictureService bannerPictureService;

    @Override
    public List<Banner> find(Optional<String> placeId, Optional<Double> minLatitude, Optional<Double> maxLatitude,
                             Optional<Double> minLongitude, Optional<Double> maxLongitude,
                             Optional<BannerSortOrder> sortBy, Direction dir, int offset, int limit) {
        String queryString = "SELECT DISTINCT b FROM Banner b";
        if (placeId.isPresent()) {
            queryString += " JOIN b.startPlaces p";
        }
        queryString += " LEFT JOIN FETCH b.startPlaces p2 LEFT JOIN FETCH p2.information WHERE true = true";
        if (placeId.isPresent()) {
            queryString += " AND p.id = :placeId";
        }
        if (minLatitude.isPresent()) {
            queryString += " AND b.startLatitude BETWEEN :minLatitude AND :maxLatitude "
                + "AND b.startLongitude BETWEEN :minLongitude AND :maxLongitude";
        }
        if (sortBy.isPresent()) {
            switch (sortBy.get()) {
                case created:
                    queryString += " ORDER BY b.created " + dir.toString();
                    break;
            }
        }
        TypedQuery<Banner> query = entityManager.createQuery(queryString, Banner.class);
        if (placeId.isPresent()) {
            query.setParameter("placeId", placeId.get());
        }
        if (minLatitude.isPresent()) {
            query.setParameter("minLatitude", minLatitude.get());
            query.setParameter("maxLatitude", maxLatitude.get());
            query.setParameter("minLongitude", minLongitude.get());
            query.setParameter("maxLongitude", maxLongitude.get());
        }
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public Optional<Banner> findByIdWithDetails(long id) {
        EntityGraph<Banner> bannerGraph = entityManager.createEntityGraph(Banner.class);
        bannerGraph.addSubgraph("missions").addSubgraph("steps").addAttributeNodes("poi");
        Optional<Banner> banner = Optional
            .ofNullable(entityManager.find(Banner.class, id, Map.of(EntityGraphType.LOAD.toString(), bannerGraph)));
        if (banner.isPresent()) {
            bannerPictureService.refresh(banner.get());
        }
        return banner;
    }

    @Override
    public long save(BannerDto bannerDto) {
        Collection<String> missionIds = Collections2.transform(bannerDto.missions.values(),
            missionDto -> missionDto.id);
        missionService.verifyAvailability(missionIds);
        Banner banner = new Banner();
        banner.setTitle(bannerDto.title);
        banner.setDescription(bannerDto.description);
        banner.setNumberOfMissions(bannerDto.numberOfMissions);
        banner.setCreated(Instant.now());
        entityManager.persist(banner);
        banner.getMissions().clear();
        banner.getMissions().putAll(Maps.transformValues(bannerDto.missions,
            missionDto -> entityManager.getReference(Mission.class, missionDto.id)));
        calculateData(banner);
        return banner.getId();
    }

    /**
     * Calculates derived data of a banner.
     *
     * @param banner Banner.
     */
    private void calculateData(Banner banner) {
        Double startLatitude = null;
        Double startLongitude = null;
        Double prevLatitude = null;
        Double prevLongitude = null;
        double distance = 0;
        boolean complete = banner.getMissions().size() == banner.getNumberOfMissions();
        boolean online = complete;

        for (Mission mission : banner.getMissions().values()) {
            online &= mission.isOnline();
            if (mission.getType() == null) {
                complete = false;
            }
            for (MissionStep step : mission.getSteps()) {
                if (step.getPoi() != null && step.getPoi().getLatitude() != null) {
                    if (prevLatitude != null) {
                        distance += getDistance(startLatitude, startLongitude, prevLatitude, prevLongitude);
                    }
                    if (startLatitude == null) {
                        startLatitude = step.getPoi().getLatitude();
                        startLongitude = step.getPoi().getLongitude();
                    }
                    prevLatitude = step.getPoi().getLatitude();
                    prevLongitude = step.getPoi().getLongitude();
                }
            }
        }
        banner.setStartLatitude(startLatitude);
        banner.setStartLongitude(startLongitude);
        banner.setComplete(complete);
        banner.setOnline(online);
        if (startLatitude != null && banner.getStartPlaces().isEmpty()) {
            Collection<Place> startPlaces = geocodingService.getPlaces(startLatitude, startLongitude);
            banner.getStartPlaces().clear();
            banner.getStartPlaces().addAll(startPlaces);
        }
        banner.setLengthMeters(distance);
        bannerPictureService.refresh(banner);
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
