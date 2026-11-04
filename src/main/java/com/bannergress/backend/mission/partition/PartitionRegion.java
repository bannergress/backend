package com.bannergress.backend.mission.partition;

import org.locationtech.jts.geom.Point;

import java.util.Collection;

public record PartitionRegion(Envelope envelope, Collection<Point> points) {
}
