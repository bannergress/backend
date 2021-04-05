package com.bannergress.backend.services;

import com.bannergress.backend.dto.BannerDto;
import com.bannergress.backend.entities.Banner;

import java.util.List;
import java.util.Optional;

/**
 * Service for banner-related tasks.
 */
public interface BannerService {
    /**
     * Finds banners.
     *
     * @param placeId      Optional place ID.
     * @param minLatitude  Optional minimum latitude.
     * @param maxLatitude  Optional maximum latitude.
     * @param minLongitude Optional minimum longitude.
     * @param maxLongitude Optional maximum longitude.
     * @param offset       Offset of the first result.
     * @param limit        Maximum number of results.
     * @return Banners that were found.
     */
    List<Banner> find(Optional<String> placeId, Optional<Double> minLatitude, Optional<Double> maxLatitude,
                      Optional<Double> minLongitude, Optional<Double> maxLongitude, int offset, int limit);

    /**
     * Finds a banner by its internal ID, including details down to mission level.
     *
     * @param id Internal ID.
     * @return Banner.
     */
    Optional<Banner> findByIdWithDetails(long id);

    /**
     * Creates a new banner. Banner missions must not be used by any other banner.
     *
     * @param bannerDto Banner DTO.
     * @return ID of the newly created banner.
     */
    long save(BannerDto bannerDto);
}
