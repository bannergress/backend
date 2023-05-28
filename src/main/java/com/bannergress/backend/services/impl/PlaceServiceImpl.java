package com.bannergress.backend.services.impl;

import com.bannergress.backend.entities.Banner;
import com.bannergress.backend.entities.Place;
import com.bannergress.backend.entities.PlaceCoordinate;
import com.bannergress.backend.entities.PlaceInformation;
import com.bannergress.backend.enums.PlaceSortOrder;
import com.bannergress.backend.enums.PlaceType;
import com.bannergress.backend.services.GeocodingService;
import com.bannergress.backend.services.PlaceService;
import com.bannergress.backend.utils.SlugGenerator;
import com.google.common.collect.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.hibernate.Session;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.bannergress.backend.utils.Spatial.getLatitude;
import static com.bannergress.backend.utils.Spatial.getLongitude;

/**
 * Default implementation of {@link PlaceService}.
 */
@Service
@Transactional
public class PlaceServiceImpl implements PlaceService {
    private static final String DEFAULT_LANGUAGE = "en";

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private GeocodingService geocodingService;

    @Autowired
    private SlugGenerator slugGenerator;

    @Override
    public List<Place> findUsedPlaces(final Optional<String> parentPlaceSlug, final Optional<String> queryString,
                                      final Optional<PlaceType> type, PlaceSortOrder orderBy, Direction orderDirection,
                                      int offset, Optional<Integer> limit) {
        String baseFragment = "SELECT DISTINCT p FROM Banner b JOIN b.startPlaces p"
            + (queryString.isPresent() ? " JOIN p.information i" : "")
            + (parentPlaceSlug.isPresent() ? " JOIN p.parentPlaces pp" : "") + " WHERE true = true";
        String parentPlaceFragment = parentPlaceSlug.isPresent() ? " AND pp.slug = :parentPlaceSlug" : "";
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
        Comparator<Place> comparator;
        switch (orderBy) {
            case numberOfBanners:
                comparator = Comparator.comparing(Place::getNumberOfBanners);
                break;
            default:
                throw new IllegalArgumentException(orderBy.toString());
        }
        Comparator<Place> comparatorDirected = orderDirection == Direction.ASC ? comparator : comparator.reversed();
        List<Place> resultUncollapsed = query.getResultList();
        List<Place> resultCollapsed = resultUncollapsed.stream() //
            .flatMap(this::collapseChildren) //
            .distinct() //
            .sorted(comparatorDirected) //
            .skip(offset) //
            .limit(limit.orElse(Integer.MAX_VALUE)) //
            .collect(Collectors.toList());
        return preloadPlaceInformation(resultCollapsed);
    }

    private Stream<Place> collapseChildren(Place place) {
        return place.isCollapsed() ? place.getChildPlaces().stream().flatMap(this::collapseChildren) : Stream.of(place);
    }

    private List<Place> preloadPlaceInformation(List<Place> places) {
        if (!places.isEmpty()) {
            TypedQuery<Place> query = entityManager
                .createQuery("SELECT p FROM Place p LEFT JOIN FETCH p.information WHERE p IN :places", Place.class);
            query.setParameter("places", places);
            query.getResultList();
        }
        return places;
    }

    private void preloadRelatedPlaces(Collection<Place> places) {
        if (!places.isEmpty()) {
            TypedQuery<Place> query = entityManager.createQuery(
                "SELECT p FROM Place p LEFT JOIN FETCH p.banners b LEFT JOIN FETCH b.startPlaces WHERE p IN :places",
                Place.class);
            query.setParameter("places", places);
            query.getResultList();
        }
    }

    @Override
    public Optional<Place> findPlaceBySlug(String slug) {
        return entityManager.unwrap(Session.class).bySimpleNaturalId(Place.class).loadOptional(slug);
    }

    @Override
    public PlaceInformation getPlaceInformation(Place place, List<Locale.LanguageRange> languagePriorityList) {
        List<String> availableLanguages = place.getInformation().stream().map(PlaceInformation::getLanguageCode)
            .collect(Collectors.toList());
        String language = Locale.lookupTag(languagePriorityList, availableLanguages);
        return place.getInformation().stream().sorted(Comparator.comparing(p -> {
            if (p.getLanguageCode().equals(language)) {
                return 1;
            } else if (p.getLanguageCode().equals(DEFAULT_LANGUAGE)) {
                return 2;
            } else {
                return 3;
            }
        })).findFirst().orElseThrow();
    }

    @Override
    public Optional<PlaceInformation> getMostAccuratePlaceInformation(Collection<Place> places,
                                                                      List<Locale.LanguageRange> languagePriorityList) {
        return places.stream().sorted(Comparator.comparing(Place::getType).reversed()).findFirst()
            .map(place -> getPlaceInformation(place, languagePriorityList));
    }

    @Override
    public Collection<Place> getPlaces(Point point) {
        TypedQuery<Place> query = entityManager
            .createQuery("SELECT DISTINCT p FROM Place p JOIN p.coordinates c WHERE c.point = :point", Place.class);
        query.setParameter("point", point);
        List<Place> results = query.getResultList();
        if (results.isEmpty()) {
            Set<Place> places = geocodingService.getPlaces(getLatitude(point), getLongitude(point));
            return places.stream().map(p -> mergePlace(p, point)).collect(Collectors.toList());
        } else {
            return results;
        }
    }

    private Place mergePlace(Place place, Point point) {
        Place existing = entityManager.find(Place.class, place.getId());
        if (existing == null) {
            place.setSlug(deriveSlug(place));
            entityManager.persist(place);
        } else {
            place = existing;
        }
        PlaceCoordinate coordinate = new PlaceCoordinate();
        coordinate.setPoint(point);
        coordinate.setPlace(place);
        entityManager.persist(coordinate);
        return place;
    }

    private String deriveSlug(Place place) {
        String longName = place.getInformation().get(0).getLongName();
        return slugGenerator.generateSlug(longName,
            slug -> entityManager.unwrap(Session.class).bySimpleNaturalId(Place.class).loadOptional(slug).isEmpty());
    }

    @Override
    public void updatePlaces(Collection<Place> places) {
        updatePlaceHierarchy(places);
        updatePlaceInformation(places);
    }

    @Override
    public void updateAllPlaces() {
        TypedQuery<Place> query = entityManager.createQuery("SELECT p FROM Place p", Place.class);
        List<Place> places = query.getResultList();
        updatePlaces(places);
    }

    private void updatePlaceHierarchy(Collection<Place> places) {
        preloadRelatedPlaces(places);
        for (Place place : places) {
            SetMultimap<PlaceType, Place> parentCandidates = TreeMultimap.create(Comparator.reverseOrder(),
                Comparator.comparing(Place::getId));
            for (Banner banner : place.getBanners()) {
                for (Place parentCandidate : banner.getStartPlaces()) {
                    if (parentCandidate.getType().compareTo(place.getType()) < 0) {
                        parentCandidates.put(parentCandidate.getType(), parentCandidate);
                    }
                }
            }
            Set<Place> oldParents = place.getParentPlaces();
            Set<Place> newParents = parentCandidates.isEmpty() ? ImmutableSet.of()
                : Multimaps.asMap(parentCandidates).values().iterator().next();
            Set<Place> parentsToRemove = ImmutableSet.copyOf(Sets.difference(oldParents, newParents));
            Set<Place> parentsToAdd = ImmutableSet.copyOf(Sets.difference(newParents, oldParents));
            place.getParentPlaces().removeAll(parentsToRemove);
            parentsToRemove.forEach(p -> p.getChildPlaces().remove(place));
            place.getParentPlaces().addAll(parentsToAdd);
            parentsToAdd.forEach(p -> p.getChildPlaces().add(place));
        }
    }

    private void updatePlaceInformation(Collection<Place> places) {
        places.stream() //
            .sorted(Comparator.comparing(Place::getType).reversed()) //
            .forEach(place -> {
                place.setNumberOfBanners(place.getBanners().size());
                place.setCollapsed(isCollapsed(place));
            });
    }

    private boolean isCollapsed(Place place) {
        if (place.getType() == PlaceType.country) {
            return false;
        } else if (place.getBanners().isEmpty()) {
            return true;
        } else {
            Set<Place> childPlaces = place.getChildPlaces().stream().flatMap(this::collapseChildren)
                .collect(Collectors.toSet());
            return childPlaces.size() == 1
                && childPlaces.iterator().next().getBanners().containsAll(place.getBanners());
        }
    }
}
