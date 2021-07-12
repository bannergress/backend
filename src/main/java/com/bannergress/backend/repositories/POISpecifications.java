package com.bannergress.backend.repositories;

import com.bannergress.backend.entities.POI;
import com.bannergress.backend.entities.POI_;
import org.hibernate.spatial.predicate.JTSSpatialPredicates;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications for POI.
 */
public class POISpecifications {
    public static Specification<POI> intersects(Geometry geometry) {
        return (poi, cq, cb) -> JTSSpatialPredicates.intersects(cb, poi.get(POI_.point), geometry);
    }
}
