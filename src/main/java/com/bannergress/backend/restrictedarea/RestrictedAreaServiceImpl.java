package com.bannergress.backend.restrictedarea;

import com.bannergress.backend.entities.Banner;
import com.bannergress.backend.entities.MissionStep;
import com.bannergress.backend.entities.POI;
import com.bannergress.backend.enums.POIType;
import com.bannergress.backend.repositories.POIRepository;
import com.bannergress.backend.repositories.POISpecifications;
import com.bannergress.backend.services.BannerService;
import com.bannergress.backend.utils.Geography;
import com.google.common.collect.ImmutableSet;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.bannergress.backend.restrictedarea.RestrictedAreaSpecifications.relevantAreaIntersects;

/**
 * Default implementation of {@link RestrictedAreaService}.
 */
@Service
@Transactional
public class RestrictedAreaServiceImpl implements RestrictedAreaService {
    @Autowired
    private RestrictedAreaRepository repository;

    @Autowired
    private POIRepository poiRepository;

    @Autowired
    private BannerService bannerService;

    /** Ingress interaction radius in meters. */
    private final int INGRESS_INTERACTION_RADIUS_METERS = 40;

    @Override
    public Optional<RestrictedArea> findByUuid(UUID uuid) {
        return repository.findById(uuid);
    }

    @Override
    public UUID create(RestrictedAreaDto restrictedAreaDto) {
        RestrictedArea restrictedArea = new RestrictedArea();
        updateAttributes(restrictedArea, restrictedAreaDto);
        return repository.save(restrictedArea).getUuid();
    }

    @Override
    public void update(UUID uuid, RestrictedAreaDto restrictedAreaDto) {
        RestrictedArea restrictedArea = repository.findById(uuid).get();
        updateAttributes(restrictedArea, restrictedAreaDto);
    }

    private void updateAttributes(RestrictedArea restrictedArea, RestrictedAreaDto restrictedAreaDto) {
        restrictedArea.setTitle(restrictedAreaDto.getTitle());
        restrictedArea.setDescription(restrictedAreaDto.getDescription());
        restrictedArea.setRestrictions(restrictedAreaDto.getRestrictions());
        restrictedArea.setArea(restrictedAreaDto.getArea());
        MultiPolygon ingressRelevantArea = Geography.bufferMeters(restrictedAreaDto.getArea(), -INGRESS_INTERACTION_RADIUS_METERS);
        restrictedArea.setIngressRelevantArea(ingressRelevantArea);
        List<POI> pois = poiRepository.findAll(POISpecifications.intersects(ingressRelevantArea));
        restrictedArea.setPois(ImmutableSet.copyOf(pois));
    }

    @PostConstruct
    void test() {
        List<RestrictedArea> test = repository.findAll();
        RestrictedArea restrictedArea = test.get(0);
        MultiPolygon ingressRelevantArea = Geography.bufferMeters(restrictedArea.getArea(),
            -INGRESS_INTERACTION_RADIUS_METERS);
        restrictedArea.setIngressRelevantArea(ingressRelevantArea);
        repository.save(restrictedArea);
    }

    @Override
    public List<RestrictedArea> findByBannerSlug(String bannerSlug) {
        Banner banner = bannerService.findBySlugWithDetails(bannerSlug).get();
        List<Point> points = banner.getMissions().values().stream() //
            .flatMap(mission -> mission.getSteps().stream()) //
            .map(MissionStep::getPoi) //
            .filter(poi -> poi != null && poi.getType() != POIType.unavailable && poi.getPoint() != null) //
            .map(POI::getPoint) //
            .toList();
        MultiPoint geometry = Geography.createMultiPoint(points);
        return repository.findAll(relevantAreaIntersects(geometry));
    }
}
