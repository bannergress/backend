package com.bannergress.backend.services.impl;

import com.bannergress.backend.entities.Place;
import com.bannergress.backend.enums.PlaceType;
import com.bannergress.backend.services.PlaceService;
import com.google.common.collect.ImmutableSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Optional;

/**
 * Default implementation of {@link PlaceService}.
 */
@Service
@Transactional
public class PlaceServiceImpl implements PlaceService {
    @Autowired
    private EntityManager entityManager;

    @Override
    public Collection<Place> findUsedPlaces(Optional<String> parentPlaceId, PlaceType type) {
        TypedQuery<Place> query;
        if (parentPlaceId.isEmpty()) {
            query = entityManager.createQuery("SELECT DISTINCT p FROM Banner b JOIN b.startPlaces p "
                + "LEFT JOIN FETCH p.information WHERE p.type = :type", Place.class);
        } else {
            query = entityManager.createQuery("SELECT DISTINCT p FROM Banner b JOIN b.startPlaces p "
                + "JOIN b.startPlaces p2 LEFT JOIN FETCH p.information "
                + "WHERE p.type = :type AND p2.id = :parentPlaceId", Place.class);
            query.setParameter("parentPlaceId", parentPlaceId);
        }
        query.setParameter("type", type);
        return ImmutableSet.copyOf(query.getResultList());
    }
}
