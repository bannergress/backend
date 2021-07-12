package com.bannergress.backend.repositories;

import com.bannergress.backend.entities.POI;
import com.bannergress.backend.entities.POI_;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.spatial.criteria.JTSSpatialCriteriaBuilder;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications for POI.
 */
public class POISpecifications {
    public static Specification<POI> intersects(Geometry geometry) {
        return (poi, cq, cb) -> ((HibernateCriteriaBuilder) cb).unwrap(JTSSpatialCriteriaBuilder.class)
            .intersects(poi.get(POI_.point), geometry);
    }
}
