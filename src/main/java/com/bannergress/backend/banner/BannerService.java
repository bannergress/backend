package com.bannergress.backend.banner;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for banner-related tasks.
 */
public interface BannerService {
    /**
     * @return List of all banner slugs.
     */
    List<String> findAllSlugs();

    /**
     * Finds a banner by its slug.
     *
     * @param id Slug.
     * @return Banner.
     */
    Optional<Banner> findBySlug(String slug);

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

    /**
     * Checks whether a banner is authored by a user, i.e. of the user is author of at least one mission of the banner.
     *
     * @param slug   Banner slug.
     * @param userId User ID.
     * @return <code>true</code>, if the user is an author of the banner.
     */
    boolean hasAuthor(String slug, String userId);

    /**
     * Checks whether an edit is probably malicious.
     *
     * @param slug      Banner slug.
     * @param bannerDto Banner DTO.
     * @param userId    User ID of the editor.
     * @return <code>true</code> if the edit is probably malicious.
     */
    boolean isProbablyMaliciousEdit(String slug, BannerDto bannerDto, String userId);

    /**
     * Checks whether an edit is a mistake.
     *
     * @param slug      Banner slug.
     * @param bannerDto Banner DTO.
     * @return <code>true</code> if the edit is a mistake.
     */
    boolean isMistakeEdit(String slug, BannerDto bannerDto);
}
