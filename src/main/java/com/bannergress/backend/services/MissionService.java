package com.bannergress.backend.services;

import com.bannergress.backend.entities.Mission;
import com.bannergress.backend.enums.MissionSortOrder;
import com.bannergress.backend.exceptions.MissionAlreadyUsedException;
import org.springframework.data.domain.Sort.Direction;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Service for mission-related task.
 */
public interface MissionService {
    /**
     * Finds missions where the query matches with the title (partial search) or the author (exact search), and that are not part of any banner.
     *
     * @param query          Query string to filter the title or the author with.
     * @param orderBy        Optional sort order.
     * @param orderDirection Sort direction.
     * @param offset         Offset of the first result.
     * @param limit          Maximum number of results.
     * @return Found missions.
     */
    Collection<Mission> findUnusedMissions(String query, Optional<MissionSortOrder> orderBy, Direction orderDirection,
                                           int offset, int limit);

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
     * @param ids                   Mission IDs.
     * @param acceptableBannerSlugs If a mission ID is only used in these banners, the check passes.
     */
    void assertNotAlreadyUsedInBanners(Collection<String> ids, List<String> acceptableBannerSlugs)
        throws MissionAlreadyUsedException;
}
