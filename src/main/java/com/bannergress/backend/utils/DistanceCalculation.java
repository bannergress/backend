package com.bannergress.backend.utils;

import com.bannergress.backend.entities.Mission;
import com.bannergress.backend.entities.MissionStep;

import java.util.Collection;

public final class DistanceCalculation {
    /**
     * Calculates the length of a set of missions in meters. The distance between the missions is included.
     * If one of the missions contains hidden waypoints, the distance is a lower bound of the actual distance.
     *
     * @param missions Missions for length calculation.
     * @return Distance in meters.
     */
    public static int calculateLengthMeters(Collection<Mission> missions) {
        Double prevLatitude = null;
        Double prevLongitude = null;
        double distance = 0;

        for (Mission mission : missions) {
            for (MissionStep step : mission.getSteps()) {
                if (step.getPoi() != null) {
                    Double latitude = step.getPoi().getLatitude();
                    Double longitude = step.getPoi().getLongitude();
                    if (latitude != null) {
                        if (prevLatitude != null) {
                            distance += getDistance(latitude, longitude, prevLatitude, prevLongitude);
                        }
                        prevLatitude = latitude;
                        prevLongitude = longitude;
                    }
                }
            }
        }
        return (int) Math.round(distance);
    }

    private static Double getDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        final int radius_meters = 6_371_000;
        Double latDistance = toRad(lat2 - lat1);
        Double lonDistance = toRad(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
            + Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return radius_meters * c;
    }

    private static Double toRad(Double value) {
        return value * Math.PI / 180;
    }
}
