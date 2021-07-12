package com.bannergress.backend.restrictedarea.overpass;

import com.bannergress.backend.restrictedarea.RestrictedArea;
import com.bannergress.backend.restrictedarea.RestrictedAreaSuggestion;
import com.bannergress.backend.restrictedarea.RestrictedAreaSuggestionService;
import com.bannergress.backend.restrictedarea.RestrictionType;
import com.bannergress.backend.spatial.Spatial;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
class OverpassRestrictedAreaSuggestionService implements RestrictedAreaSuggestionService {
    @Autowired
    private OverpassApi overpassApi;

    @Autowired
    private Spatial spatial;

    private ImmutableMultimap<String, String> ignoredTags = ImmutableMultimap.<String, String>builder() //
        .putAll("boundary", "administrative", "postal_code", "political", "timezone", "land_area", "region") //
        .build();

    @Override
    public List<RestrictedAreaSuggestion> getSuggestions(double latitude, double longitude) {
        String query = String.format("[out:json];is_in(%s,%s);out tags;", latitude, longitude);
        OverpassApiResult apiResult = overpassApi.query(query);
        return apiResult.getElements().stream() //
            .filter(this::isRelevant) //
            .map(this::toSuggestion) //
            .toList();
    }

    private boolean isRelevant(OverpassGeometry geometry) {
        return geometry.getTags() == null || geometry.getTags().entrySet().stream()//
            .noneMatch(entry -> ignoredTags.containsEntry(entry.getKey(), entry.getValue()));
    }

    private RestrictedAreaSuggestion toSuggestion(OverpassGeometry geometry) {
        RestrictedAreaSuggestion result = new RestrictedAreaSuggestion();
        result.setId(geometry.getId());
        result.setTitle(getTitle(geometry));
        result.setRestrictions(getRestrictions(geometry));
        return result;
    }

    private String getTitle(OverpassGeometry geometry) {
        return geometry.getTags().getOrDefault("name", "[unnamed]");
    }

    private Set<RestrictionType> getRestrictions(OverpassGeometry geometry) {
        Map<String, String> tags = geometry.getTags();
        Builder<RestrictionType> builder = ImmutableSet.builder();
        String feeTag = tags.get("fee");
        if ("yes".equals(feeTag)) {
            builder.add(RestrictionType.monetary);
        }
        String openingHours = tags.get("opening_hours");
        if (openingHours != null && !"24/7".equals(openingHours)) {
            builder.add(RestrictionType.temporal);
        }
        String access = tags.get("access");
        if ("private".equals(access) || "no".equals(access)) {
            builder.add(RestrictionType.audience);
        }
        return builder.build();
    }

    @Override
    public RestrictedArea getTemplate(String id) {
        String query = String.format("[out:json];area(%s);nwr(pivot);out geom;", Long.parseLong(id));
        OverpassApiResult apiResult = overpassApi.query(query);
        OverpassGeometry geometry = apiResult.getElements().get(0);
        RestrictedArea result = new RestrictedArea();
        result.setTitle(getTitle(geometry));
        result.setArea(getGeometry(geometry));
        result.setRestrictions(getRestrictions(geometry));
        return result;
    }

    private MultiPolygon getGeometry(OverpassGeometry geometry) {
        if (geometry instanceof OverpassRelation relation) {
            List<LineString> ways = relation.getMembers().stream() //
                .filter(m -> m instanceof OverpassWay && (m.getRole().equals("outer") || m.getRole().equals("inner")))
                .map(m -> toLineString((OverpassWay) m)) //
                .toList();
            return polygonize(ways);
        } else if (geometry instanceof OverpassWay way) {
            List<LineString> ways = List.of(toLineString(way));
            return polygonize(ways);
        } else {
            throw new IllegalArgumentException(geometry.getClass().toString());
        }
    }

    private LineString toLineString(OverpassWay way) {
        Coordinate[] coordinates = way.getGeometry().stream() //
            .map(c -> new Coordinate(c.getLon(), c.getLat())) //
            .toArray(Coordinate[]::new);
        return spatial.getFactory().createLineString(coordinates);
    }

    private MultiPolygon polygonize(List<LineString> geometries) {
        Polygonizer polygonizer = new Polygonizer(true);
        polygonizer.add(geometries);
        if (!polygonizer.getCutEdges().isEmpty() || !polygonizer.getDangles().isEmpty()
            || !polygonizer.getInvalidRingLines().isEmpty()) {
            throw new IllegalArgumentException("Invalid geometry");
        }
        return spatial.toMultipolygon(polygonizer.getGeometry());
    }
}
