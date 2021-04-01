package com.bannergress.backend.services;

import com.bannergress.backend.dto.BannerDto;
import com.bannergress.backend.entities.Banner;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Service for banner-related tasks.
 */
public interface BannerService {
    /**
     * Finds banners that are located at a place.
     *
     * @param placeId    Place ID.
     * @param offset     Offset of the first result.
     * @param maxResults Maximum number of results.
     * @return Banners that were found.
     */
    List<Banner> findByPlace(String placeId, int offset, int maxResults);

    /**
     * Finds banners inside an area.
     *
     * @param minLatitude  Minimum latitude.
     * @param maxLatitude  Maximum latitude.
     * @param minLongitude Minimum longitude.
     * @param maxLongitude Maximum longitude.
     * @param offset       Offset of the first result.
     * @param maxResults   Maximum number of results.
     * @return Banners that were found.
     */
    Collection<Banner> findByBounds(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude,
                                    int offset, int maxResults);

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
