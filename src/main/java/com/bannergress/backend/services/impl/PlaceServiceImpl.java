package com.bannergress.backend.services.impl;

import com.bannergress.backend.entities.Place;
import com.bannergress.backend.entities.PlaceCoordinate;
import com.bannergress.backend.entities.PlaceInformation;
import com.bannergress.backend.enums.PlaceType;
import com.bannergress.backend.services.GeocodingService;
import com.bannergress.backend.services.PlaceService;
import com.google.common.collect.ImmutableSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Default implementation of {@link PlaceService}.
 */
@Service
@Transactional
public class PlaceServiceImpl implements PlaceService {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private GeocodingService geocodingService;

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
    public Optional<Place> findPlaceById(String id) {
        return Optional.ofNullable(entityManager.find(Place.class, id));
    }

    @Override
    public PlaceInformation getPlaceInformation(Place place, String languageCode) {
        // languageCode is ignored for now, we always fetch the first (english) translation
        return place.getInformation().get(0);
    }

    @Override
    public Optional<PlaceInformation> getMostAccuratePlaceInformation(Collection<Place> places,
                                                                      String languagePreference) {
        return places.stream().sorted(Comparator.comparing(Place::getType).reversed()).findFirst()
            .map(place -> getPlaceInformation(place, languagePreference));
    }

    @Override
    public Collection<Place> getPlaces(double latitude, double longitude) {
        TypedQuery<Place> query = entityManager.createQuery("SELECT DISTINCT p FROM Place p JOIN p.coordinates c"
            + " WHERE c.latitude = :latitude AND c.longitude = :longitude", Place.class);
        query.setParameter("latitude", latitude);
        query.setParameter("longitude", longitude);
        List<Place> results = query.getResultList();
        if (results.isEmpty()) {
            Collection<Place> places = geocodingService.getPlaces(latitude, longitude);
            for (Place place : places) {
                PlaceCoordinate coordinate = new PlaceCoordinate();
                coordinate.setLatitude(latitude);
                coordinate.setLongitude(longitude);
                coordinate.setPlace(place);
                entityManager.persist(coordinate);
            }
            return places;
        } else {
            return results;
        }
    }
}
