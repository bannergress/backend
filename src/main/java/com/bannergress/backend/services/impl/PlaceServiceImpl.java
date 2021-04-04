package com.bannergress.backend.services.impl;

import com.bannergress.backend.entities.Place;
import com.bannergress.backend.entities.PlaceInformation;
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
    public Collection<Place> findUsedPlaces(final Optional<String> parentPlaceId, final Optional<String> queryString,
                                            final Optional<PlaceType> type) {
        String baseFragment = parentPlaceId.isPresent()
            ? "SELECT DISTINCT p FROM Banner b JOIN b.startPlaces p JOIN b.startPlaces p2 "
                + "LEFT JOIN FETCH p.information i WHERE p2.id = :parentPlaceId"
            : "SELECT DISTINCT p FROM Banner b JOIN b.startPlaces p "
                + "LEFT JOIN FETCH p.information i WHERE true = true";
        String typeFragment = type.isPresent() ? " AND p.type = :type" : "";
        String queryStringFragment = queryString.isPresent() ? " AND LOWER(i.longName) LIKE :queryString" : "";
        TypedQuery<Place> query = entityManager.createQuery(baseFragment + typeFragment + queryStringFragment,
            Place.class);
        if (type.isPresent()) {
            query.setParameter("type", type.get());
        }
        if (parentPlaceId.isPresent()) {
            query.setParameter("parentPlaceId", parentPlaceId.get());
        }
        if (queryString.isPresent()) {
            // Right now, the query string filters only on the long name of the place.
            // In the future, it might also filter on other aspects, like short name.
            query.setParameter("queryString", "%" + queryString.get().toLowerCase() + "%");
        }
        return ImmutableSet.copyOf(query.getResultList());
    }

    @Override
    public PlaceInformation getPlaceInformation(Place place, String languageCode) {
        // languageCode is ignored for now, we always fetch the first (english) translation
        return place.getInformation().get(0);
    }
}
