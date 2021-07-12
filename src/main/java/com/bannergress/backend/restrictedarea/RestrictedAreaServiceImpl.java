package com.bannergress.backend.restrictedarea;

import com.bannergress.backend.banner.Banner;
import com.bannergress.backend.banner.BannerService;
import com.bannergress.backend.mission.step.MissionStep;
import com.bannergress.backend.poi.POI;
import com.bannergress.backend.poi.POIType;
import com.bannergress.backend.spatial.Spatial;
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
    private BannerService bannerService;

    @Autowired
    private Spatial spatial;

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
        MultiPolygon ingressRelevantArea = spatial.bufferMeters(restrictedAreaDto.getArea(),
            -INGRESS_INTERACTION_RADIUS_METERS);
        restrictedArea.setIngressRelevantArea(ingressRelevantArea);
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
        MultiPoint geometry = spatial.createMultiPoint(points);
        return repository.findAll(relevantAreaIntersects(geometry));
    }
}
