package com.bannergress.backend.services;

import com.bannergress.backend.dto.IntelMissionDetails;
import com.bannergress.backend.dto.IntelMissionSummary;
import com.bannergress.backend.dto.IntelTopMissionsForPortal;
import com.bannergress.backend.dto.IntelTopMissionsInBounds;
import com.bannergress.backend.entities.Mission;

import javax.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * Service for imports from Intel.
 */
public interface IntelImportService {
    /**
     * Imports a mission from intel-based data.
     *
     * @param data            Intel response data.
     * @param setStatusOnline Set status of mission to online.
     * @return Imported mission.
     */
    Mission importMission(IntelMissionDetails data, boolean setStatusOnline);

    /**
     * Imports missions from intel-based data.
     *
     * @param data Intel request and response data.
     * @return Imported missions.
     */
    Collection<Mission> importTopMissionsInBounds(IntelTopMissionsInBounds data);

    /**
     * Imports missions from intel-based data.
     *
     * @param data Intel request and response data.
     * @return Imported missions.
     */
    Collection<Mission> importTopMissionsForPortal(IntelTopMissionsForPortal data);

    /**
     * Imports missions from intel-based data.
     *
     * @param summaries Intel summary data.
     * @return Imported missions.
     */
    Collection<Mission> importMissionSummaries(List<@Valid IntelMissionSummary> summaries);
}
