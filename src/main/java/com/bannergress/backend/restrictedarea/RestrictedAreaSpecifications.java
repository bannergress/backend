package com.bannergress.backend.restrictedarea;

import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.spatial.criteria.JTSSpatialCriteriaBuilder;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.domain.Specification;

public interface RestrictedAreaSpecifications {
    public static Specification<RestrictedArea> relevantAreaIntersects(Geometry geometry) {
        return (restrictedArea, cq, cb) -> ((HibernateCriteriaBuilder) cb).unwrap(JTSSpatialCriteriaBuilder.class)
            .intersects(restrictedArea.get(RestrictedArea_.ingressRelevantArea), geometry);
    }
}
