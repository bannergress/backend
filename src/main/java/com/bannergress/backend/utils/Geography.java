package com.bannergress.backend.utils;

import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.api.referencing.operation.TransformException;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.*;

import java.util.List;

public class Geography {
    private static final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
        4326);

    public static Point createPointLatLon(double latitude, double longitude) {
        return factory.createPoint(new CoordinateXY(longitude, latitude));
    }

    public static MultiPoint createMultiPoint(List<Point> points) {
        return factory.createMultiPoint(points.toArray(Point[]::new));
    }

    public static MultiPolygon createMultiPolygon(Polygon... polygons) {
        return factory.createMultiPolygon(polygons);
    }

    public static double lat(Point point) {
        return point.getY();
    }

    public static double lon(Point point) {
        return point.getX();
    }

    public static MultiPolygon bufferMeters(MultiPolygon geometry, double bufferInMeters) {
        AutoCRSTransformer transformer = AutoCRSTransformer.forGeometry(geometry);
        Geometry transformed = transformer.transformToAutoCRS(geometry);
        Geometry transformedBuffered = transformed.buffer(bufferInMeters);
        Geometry result = transformer.transformToWGS84(transformedBuffered);
        return toMultipolygon(result);
    }

    public static MultiPolygon toMultipolygon(Geometry geometry) {
        Geometry union = geometry.union();
        if (union instanceof MultiPolygon multiPolygon) {
            return multiPolygon;
        } else if (union instanceof Polygon polygon) {
            return Geography.createMultiPolygon(polygon);
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
