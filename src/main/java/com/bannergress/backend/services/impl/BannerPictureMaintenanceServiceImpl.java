package com.bannergress.backend.services.impl;

import com.bannergress.backend.services.BannerPictureMaintenanceService;
import com.bannergress.backend.services.BannerPictureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Implements {@link BannerPictureService} to call
 * {@link BannerPictureService#removeExpired()} at a fixed rate to remove orphaned banner
 * pictures.
 */
@Service
@EnableScheduling
public class BannerPictureMaintenanceServiceImpl implements BannerPictureMaintenanceService {

    @Autowired
    BannerPictureService bannerPictureService;

    @Override
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    public void removeExpired() {
        bannerPictureService.removeExpired();
    }

}
