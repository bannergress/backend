package com.bannergress.backend.utils;

import com.bannergress.backend.entities.Mission;
import com.bannergress.backend.entities.MissionStep;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Point;

import java.util.Collection;

import static com.bannergress.backend.utils.Spatial.getLatitude;
import static com.bannergress.backend.utils.Spatial.getLongitude;

public final class DistanceCalculation {
    /**
     * Calculates the length of a set of missions in meters. The distance between the missions is included.
     * If one of the missions contains hidden waypoints, the distance is a lower bound of the actual distance.
     *
     * @param missions Missions for length calculation.
     * @return Distance in meters.
     */
    public static int calculateLengthMeters(Collection<Mission> missions) {
        Point prevPoint = null;
        double distance = 0;

        for (Mission mission : missions) {
            for (MissionStep step : mission.getSteps()) {
                if (step.getPoi() != null) {
                    Point point = step.getPoi().getPoint();
                    if (point != null) {
                        if (prevPoint != null) {
                            distance += getDistance(getLatitude(point), getLongitude(point), getLatitude(prevPoint),
                                getLongitude(prevPoint));
                        }
                        prevPoint = point;
                    }
                }
            }
        }
        return (int) Math.round(distance);
    }

    private static double getDistance(double lat1, double lon1, double lat2, double lon2) {
        GeodeticCalculator gc = new GeodeticCalculator(DefaultGeographicCRS.WGS84);
        gc.setStartingGeographicPoint(lon1, lat1);
        gc.setDestinationGeographicPoint(lon2, lat2);
        return gc.getOrthodromicDistance();
    }
}
