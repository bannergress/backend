package com.bannergress.backend.banner.picture;

/**
 * Service for banner picture tasks.
 */
public interface BannerPictureMaintenanceService {
    /**
     * Removes expired banner pictures.
     */
    void removeExpired();
}
