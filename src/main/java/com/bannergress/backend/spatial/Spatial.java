package com.bannergress.backend.spatial;

import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.api.referencing.operation.TransformException;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.*;
import org.springframework.stereotype.Component;

import java.util.List;

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

    public GeometryFactory getFactory() {
        return factory;
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

    public MultiPoint createMultiPoint(List<Point> points) {
        return factory.createMultiPoint(points.toArray(Point[]::new));
    }

    public MultiPolygon createMultiPolygon(Polygon... polygons) {
        return factory.createMultiPolygon(polygons);
    }

    public MultiPolygon bufferMeters(MultiPolygon geometry, double bufferInMeters) {
        AutoCRSTransformer transformer = AutoCRSTransformer.forGeometry(geometry);
        Geometry transformed = transformer.transformToAutoCRS(geometry);
        Geometry transformedBuffered = transformed.buffer(bufferInMeters);
        Geometry result = transformer.transformToWGS84(transformedBuffered);
        return toMultipolygon(result);
    }

    public MultiPolygon toMultipolygon(Geometry geometry) {
        Geometry union = geometry.union();
        if (union instanceof MultiPolygon multiPolygon) {
            return multiPolygon;
        } else if (union instanceof Polygon polygon) {
            return createMultiPolygon(polygon);
        } else {
            throw new IllegalArgumentException("No polygonal geometry");
        }
    }

    private interface AutoCRSTransformer {
        <T extends Geometry> T transformToAutoCRS(T geometry);

        <T extends Geometry> T transformToWGS84(T geometry);

        static AutoCRSTransformer forGeometry(Geometry geometry) {
            try {
                Point centroid = geometry.getCentroid();
                CoordinateReferenceSystem wgs84 = DefaultGeographicCRS.WGS84;
                CoordinateReferenceSystem autoCRS = CRS
                    .decode(String.format("AUTO:42001,%s,%s", centroid.getX(), centroid.getY()));
                MathTransform toAutoCRS = CRS.findMathTransform(wgs84, autoCRS);
                MathTransform toWGS84 = CRS.findMathTransform(autoCRS, wgs84);
                return new AutoCRSTransformer() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public <T extends Geometry> T transformToAutoCRS(T geometry) {
                        try {
                            return (T) JTS.transform(geometry, toAutoCRS);
                        } catch (TransformException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    public <T extends Geometry> T transformToWGS84(T geometry) {
                        try {
                            return (T) JTS.transform(geometry, toWGS84);
                        } catch (TransformException e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
            } catch (FactoryException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
