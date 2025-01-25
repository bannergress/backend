package com.bannergress.backend.mission.intel;

import com.bannergress.backend.mission.Mission;

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
    Collection<Mission> importMissionSummaries(List<com.bannergress.backend.mission.intel.IntelMissionSummary> summaries);
}
