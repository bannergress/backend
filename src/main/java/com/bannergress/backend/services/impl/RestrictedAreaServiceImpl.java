package com.bannergress.backend.services.impl;

import com.bannergress.backend.dto.RestrictedAreaDto;
import com.bannergress.backend.entities.POI;
import com.bannergress.backend.entities.RestrictedArea;
import com.bannergress.backend.repositories.POIRepository;
import com.bannergress.backend.repositories.POISpecifications;
import com.bannergress.backend.repositories.RestrictedAreaRepository;
import com.bannergress.backend.services.RestrictedAreaService;
import com.bannergress.backend.utils.Geography;
import com.google.common.collect.ImmutableSet;
import jakarta.transaction.Transactional;
import org.locationtech.jts.geom.MultiPolygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        MultiPolygon area = Geography.toMultipolygon(restrictedAreaDto.area);
        restrictedArea.setArea(area);
        MultiPolygon ingressRelevantArea = Geography.bufferMeters(area, -INGRESS_INTERACTION_RADIUS_METERS);
        List<POI> poi = poiRepository.findAll(POISpecifications.intersects(ingressRelevantArea));
        restrictedArea.setPois(ImmutableSet.copyOf(poi));
    }
}
