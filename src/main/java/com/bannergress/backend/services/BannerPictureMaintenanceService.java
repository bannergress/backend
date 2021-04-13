package com.bannergress.backend.services;

/**
 * Service for banner picture tasks.
 */
public interface BannerPictureMaintenanceService {
    /**
     * Removes expired banner pictures.
     */
    void removeExpired();
}
