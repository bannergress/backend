package com.bannergress.backend.services;

import com.bannergress.backend.dto.IntelMissionDetails;
import com.bannergress.backend.dto.IntelMissionSummary;
import com.bannergress.backend.entities.Mission;
import jakarta.validation.Valid;

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
     * @param summaries Intel summary data.
     * @return Imported missions.
     */
    Collection<Mission> importMissionSummaries(List<@Valid IntelMissionSummary> summaries);
}
