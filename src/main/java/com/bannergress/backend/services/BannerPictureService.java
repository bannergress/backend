package com.bannergress.backend.services;

import com.bannergress.backend.entities.Banner;
import com.bannergress.backend.entities.BannerPicture;

import java.util.Optional;

/**
 * Service for banner picture tasks.
 */
public interface BannerPictureService {
    /**
     * Refreshes a banner picture.
     *
     * @param banner Banner whose picture should be refreshed.
     */
    void refresh(Banner banner);

    Optional<BannerPicture> findByHash(String hash);
}
