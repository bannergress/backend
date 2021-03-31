package com.bannergress.backend.services;

import com.bannergress.backend.dto.IntelMissionDetails;
import com.bannergress.backend.dto.IntelTopMissionsInBounds;
import com.bannergress.backend.entities.Mission;

import java.util.Collection;
import java.util.Optional;

/**
 * Service for mission-related task.
 */
public interface MissionService {
    /**
     * Imports a mission from intel-based data.
     *
     * @param data Intel response data.
     */
    void importMission(IntelMissionDetails data);

    /**
     * Imports missions from intel-based data.
     *
     * @param data Intel request and response data.
     */
    void importTopMissionsInBounds(IntelTopMissionsInBounds data);

    /**
     * Finds missions contain a string in their titles and that are not part of any banner.
     *
     * @param query      Query string to filter the title with.
     * @param maxResults Maximum number of results.
     * @return Found missions.
     */
    Collection<Mission> findUnusedMissions(String query, int maxResults);

    /**
     * Find mission by ID.
     *
     * @param id Niantic mission ID.
     * @return Mission.
     */
    Optional<Mission> findById(String id);

    /**
     * Finds missions by IDs.
     *
     * @param ids Niantic mission IDs.
     * @return Mission.
     */
    Collection<Mission> findByIds(Collection<String> ids);

    /**
     * Verifies that a collection of missions is available for use in a banner. Throws if at least one of the missions is not available.
     *
     * @param ids Mission IDs.
     */
    void verifyAvailability(Collection<String> ids);
}
