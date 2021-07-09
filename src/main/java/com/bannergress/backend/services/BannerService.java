package com.bannergress.backend.services;

import com.bannergress.backend.dto.BannerDto;
import com.bannergress.backend.entities.Banner;
import com.bannergress.backend.enums.BannerSortOrder;
import com.bannergress.backend.exceptions.MissionAlreadyUsedException;
import org.springframework.data.domain.Sort.Direction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for banner-related tasks.
 */
public interface BannerService {
    /**
     * Finds banners.
     *
     * @param placeSlug            Optional place Slug.
     * @param minLatitude          Optional minimum latitude.
     * @param maxLatitude          Optional maximum latitude.
     * @param minLongitude         Optional minimum longitude.
     * @param maxLongitude         Optional maximum longitude.
     * @param query                Optional query string.
     * @param missionId            Optional ID of mission which has to be contained in banner.
     * @param onlyOfficialMissions Whether to only include official mission accounts.
     * @param author               Optional author of one of the banner missions.
     * @param orderBy              Optional sort order.
     * @param orderDirection       Sort direction.
     * @param offset               Offset of the first result.
     * @param limit                Maximum number of results.
     * @return Banners that were found.
     */
    List<Banner> find(Optional<String> placeSlug, Optional<Double> minLatitude, Optional<Double> maxLatitude,
                      Optional<Double> minLongitude, Optional<Double> maxLongitude, Optional<String> query,
                      Optional<String> missionId, boolean onlyOfficialMissions, Optional<String> author,
                      Optional<BannerSortOrder> orderBy, Direction orderDirection, int offset, int limit);

    /**
     * @return List of all banner slugs.
     */
    List<String> findAllSlugs();

    /**
     * Finds a banner by its slug, including details down to mission level.
     *
     * @param id Slug.
     * @return Banner.
     */
    Optional<Banner> findBySlugWithDetails(String slug);

    /**
     * Creates a new banner. Banner missions must not be used by any other banner.
     *
     * @param bannerDto Banner DTO.
     * @return Slug of the newly created banner.
     * @throws MissionAlreadyUsedException If a mission is already used by another banner.
     */
    String create(BannerDto bannerDto) throws MissionAlreadyUsedException;

    /**
     * Generates a preview of a new banner without actually persisting it. Banner missions must not be used by any other banner.
     *
     * @param bannerDto Banner DTO.
     * @return Newly created banner.
     * @throws MissionAlreadyUsedException If a mission is already used by another banner.
     */
    Banner generatePreview(BannerDto bannerDto) throws MissionAlreadyUsedException;

    /**
     * Updates an existing banner.
     *
     * @param bannerDto Banner DTO.
     * @throws MissionAlreadyUsedException If a mission is already used by another banner.
     */
    void update(String slug, BannerDto bannerDto) throws MissionAlreadyUsedException;

    /**
     * Deletes a banner by slug.
     *
     * @param slug Slug to delete.
     */
    void deleteBySlug(String slug);

    /**
     * Calculates derived data of a banner.
     *
     * @param banner Banner.
     */
    void calculateData(Banner banner);

    /**
     * @return UUIDs of all banners.
     */
    public List<UUID> findAllUUIDs();

    /**
     * Calculates derived data the banner identified by UUID.
     *
     * @param UUID uuid.
     */
    void calculateBanner(UUID uuid);
}
