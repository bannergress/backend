package com.bannergress.backend.services;

import com.bannergress.backend.entities.Banner;
import com.bannergress.backend.enums.BannerListType;
import com.bannergress.backend.enums.BannerSortOrder;
import org.springframework.data.domain.Sort.Direction;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Service for banner searching.
 */
public interface BannerSearchService {
    /**
     * Finds banners.
     *
     * @param placeSlug            Optional place Slug.
     * @param minLatitude          Optional minimum latitude.
     * @param maxLatitude          Optional maximum latitude.
     * @param minLongitude         Optional minimum longitude.
     * @param maxLongitude         Optional maximum longitude.
     * @param query                Optional query string.
     * @param queryAuthor          Should the query string be extended to author information?
     * @param missionId            Optional ID of mission which has to be contained in banner.
     * @param onlyOfficialMissions Whether to only include official mission accounts.
     * @param author               Optional author of one of the banner missions.
     * @param listTypes            Optional types of lists the banner is on.
     * @param user                 Optional User ID (needed for listTypes).
     * @param online               Optional online status.
     * @param orderBy              Optional sort order.
     * @param orderDirection       Sort direction.
     * @param proximityLatitude    Optional reference latitude for proximity sorting.
     * @param proximityLongitude   Optional reference longitude for proximity sorting.
     * @param offset               Offset of the first result.
     * @param limit                Maximum number of results.
     * @return Banners that were found.
     */
    List<Banner> find(Optional<String> placeSlug, Optional<Double> minLatitude, Optional<Double> maxLatitude,
                      Optional<Double> minLongitude, Optional<Double> maxLongitude, Optional<String> query,
                      boolean queryAuthor, Optional<String> missionId, boolean onlyOfficialMissions,
                      Optional<String> author, Optional<Collection<BannerListType>> listTypes, Optional<String> userId,
                      Optional<Boolean> online, Optional<BannerSortOrder> orderBy, Direction orderDirection,
                      Optional<Double> proximityLatitude, Optional<Double> proximityLongitude, int offset, int limit);

    /** Updates the search index. */
    void updateIndex();
}
