package com.bannergress.backend.utils;

import org.locationtech.jts.geom.*;
import org.springframework.stereotype.Component;

/**
 * Spatial utilities.
 */
@Component
public class Spatial {
    /** SRID for WGS-84 ellipsoid (aka GPS coordinates). */
    private static final int SRID_WGS84 = 4326;

    /** Factory for WGS 84 geometries. */
    private final GeometryFactory factory = new GeometryFactory(new PrecisionModel(), SRID_WGS84);

    /**
     * Creates a point out of latitude and longitude coordinates.
     *
     * @param latitude  Latitude, can be <code>null</code>.
     * @param longitude Longitude, can be <code>null</code>.
     * @return point (<code>null</code> if either latitude or longitude is <code>null</code>).
     */
    public Point createPoint(Double latitude, double longitude) {
        return factory.createPoint(new Coordinate(longitude, latitude));
    }

    /**
     * Creates a geometry that represents a bounding box.
     *
     * @param minLatitude  Minimum latitude.
     * @param maxLatitude  Maximum latitude.
     * @param minLongitude Minimum longitude.
     * @param maxLongitude Maximum longitude.
     * @return
     */
    public Geometry createBoundingBox(double minLatitude, double maxLatitude, double minLongitude,
                                      double maxLongitude) {
        Envelope envelope = new Envelope(minLongitude, maxLongitude, minLatitude, maxLatitude);
        return factory.toGeometry(envelope);
    }

    /**
     * Returns the latitude of a point.
     *
     * @param point Point, can be <code>null</code>.
     * @return latitude (<code>null</code> if point is <code>null</code>).
     */
    public static Double getLatitude(Point point) {
        return point == null ? null : point.getY();
    }

    /**
     * Returns the longitude of a point.
     *
     * @param point Point, can be <code>null</code>.
     * @return longitude (<code>null</code> if point is <code>null</code>).
     */
    public static Double getLongitude(Point point) {
        return point == null ? null : point.getX();
    }
}
