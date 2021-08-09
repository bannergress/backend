package com.bannergress.backend.services;

import com.bannergress.backend.entities.BannerSettings;
import com.bannergress.backend.enums.BannerListType;

import java.util.Collection;
import java.util.List;

/**
 * Service for banner settings related tasks.
 */
public interface BannerSettingsService {
    /**
     * Adds a banner to one of the users lists.
     *
     * @param userId     ID of the user.
     * @param bannerSlug Slug of the banner.
     */
    void addBannerToList(String userId, String bannerSlug, BannerListType listType);

    /**
     * Retrieves banner settings user and a collection of banners.
     *
     * @param userId  ID of the user.
     * @param banners Canonical slugs of banner for which to retrieve the settings.
     * @return Banner settings.
     */
    List<BannerSettings> getBannerSettings(String userId, Collection<String> bannerSlugs);
}
