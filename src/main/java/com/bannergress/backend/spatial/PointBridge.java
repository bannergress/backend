package com.bannergress.backend.spatial;

import org.hibernate.search.engine.spatial.GeoPoint;
import org.hibernate.search.mapper.pojo.bridge.ValueBridge;
import org.hibernate.search.mapper.pojo.bridge.runtime.ValueBridgeToIndexedValueContext;
import org.locationtech.jts.geom.Point;

/**
 * Bridge between JTS Points and Hibernate Search GeoPoint.
 */
public class PointBridge implements ValueBridge<Point, GeoPoint> {
    @Override
    public GeoPoint toIndexedValue(Point value, ValueBridgeToIndexedValueContext context) {
        return value == null ? null : new GeoPoint() {
            @Override
            public double longitude() {
                return Spatial.getLongitude(value);
            }

            @Override
            public double latitude() {
                return Spatial.getLatitude(value);
            }
        };
    }
}
