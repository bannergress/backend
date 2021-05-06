package com.bannergress.backend.services.impl;

import com.bannergress.backend.entities.Place;
import com.bannergress.backend.entities.PlaceCoordinate;
import com.bannergress.backend.entities.PlaceInformation;
import com.bannergress.backend.enums.PlaceType;
import com.bannergress.backend.services.GeocodingService;
import com.bannergress.backend.services.PlaceService;
import com.bannergress.backend.utils.SlugGenerator;
import com.google.common.collect.ImmutableSet;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Autowired
    private SlugGenerator slugGenerator;

    @Override
    public Collection<Place> findUsedPlaces(final Optional<String> parentPlaceSlug, final Optional<String> queryString,
                                            final Optional<PlaceType> type) {
        String baseFragment = "SELECT DISTINCT p FROM Banner b JOIN b.startPlaces p "
            + "LEFT JOIN FETCH p.information i WHERE true = true";
        String parentPlaceFragment = parentPlaceSlug.isPresent() ? " AND p.parentPlace.slug = :parentPlaceSlug" : "";
        String typeFragment = type.isPresent() ? " AND p.type = :type" : "";
        String queryStringFragment = queryString.isPresent() ? " AND LOWER(i.longName) LIKE :queryString" : "";
        TypedQuery<Place> query = entityManager
            .createQuery(baseFragment + parentPlaceFragment + typeFragment + queryStringFragment, Place.class);
        if (type.isPresent()) {
            query.setParameter("type", type.get());
        }
        if (parentPlaceSlug.isPresent()) {
            query.setParameter("parentPlaceSlug", parentPlaceSlug.get());
        }
        if (queryString.isPresent()) {
            // Right now, the query string filters only on the long name of the place.
            // In the future, it might also filter on other aspects, like short name.
            query.setParameter("queryString", "%" + queryString.get().toLowerCase() + "%");
        }
        return ImmutableSet.copyOf(query.getResultList());
    }

    @Override
    public Optional<Place> findPlaceBySlug(String slug) {
        return entityManager.unwrap(Session.class).bySimpleNaturalId(Place.class).loadOptional(slug);
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
            Optional<Place> place = geocodingService.getPlaceHierarchy(latitude, longitude);
            if (place.isPresent()) {
                mergePlace(place.get(), latitude, longitude);
            }
            return place.stream().flatMap(this::expandPlaces).collect(Collectors.toList());
        } else {
            return results;
        }
    }

    private Place mergePlace(Place place, double latitude, double longitude) {
        Place parentPlace = place.getParentPlace();
        if (parentPlace != null) {
            parentPlace = mergePlace(parentPlace, latitude, longitude);
        }
        place.setParentPlace(parentPlace);
        Place existing = entityManager.find(Place.class, place.getId());
        if (existing == null) {
            place.setSlug(deriveSlug(place));
            entityManager.persist(place);
        } else {
            place = existing;
        }
        PlaceCoordinate coordinate = new PlaceCoordinate();
        coordinate.setLatitude(latitude);
        coordinate.setLongitude(longitude);
        coordinate.setPlace(place);
        entityManager.persist(coordinate);
        return place;
    }

    private String deriveSlug(Place place) {
        String longName = place.getInformation().get(0).getLongName();
        return slugGenerator.generateSlug(longName,
            slug -> entityManager.unwrap(Session.class).bySimpleNaturalId(Place.class).loadOptional(slug).isEmpty());
    }

    private Stream<Place> expandPlaces(Place place) {
        if (place.getParentPlace() == null) {
            return Stream.of(place);
        } else {
            return Stream.concat(Stream.of(place), expandPlaces(place.getParentPlace()));
        }
    }
}
