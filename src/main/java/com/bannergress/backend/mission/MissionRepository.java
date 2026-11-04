package com.bannergress.backend.mission;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repository for missions.
 */
public interface MissionRepository extends JpaRepository<Mission, String>, JpaSpecificationExecutor<Mission> {
    @Query("""
        SELECT s.poi.point FROM Mission m JOIN m.steps s
        WHERE INDEX(s) = 0
        AND WITHIN(s.poi.point, :envelope)
        AND m.status = :status
        """)
    List<Point> findMissionStartPositionsByStatus(Geometry envelope, MissionStatus status);
}
