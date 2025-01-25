package com.bannergress.backend.banner.picture;

import com.bannergress.backend.banner.Banner;

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

    /**
     * Sets a picture as expired.
     *
     * @param picture Picture, may be <code>null</code>.
     */
    void setPictureExpired(BannerPicture picture);

    void removeExpired();
}
