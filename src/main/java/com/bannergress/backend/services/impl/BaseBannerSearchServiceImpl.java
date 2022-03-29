package com.bannergress.backend.services.impl;

import com.bannergress.backend.entities.Banner;
import com.bannergress.backend.repositories.BannerRepository;
import com.bannergress.backend.repositories.BannerSpecifications;
import com.bannergress.backend.services.BannerSearchService;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Base class for banner search implementations.
 */
abstract class BaseBannerSearchServiceImpl implements BannerSearchService {
    protected static final List<String> OFFICIAL_MISSION_AUTHORS = ImmutableList.of( //
        "MissionbyNIA", //
        "MissionsbyNIA", //
        "MissionDaysNia", //
        "MissionsNIA", //
        "MDNIA2", //
        "MDNIA", //
        "MDNIA2020" //
    );

    @Autowired
    protected BannerRepository bannerRepository;

    protected final void preloadPlaceInformation(List<Banner> banners) {
        if (!banners.isEmpty()) {
            bannerRepository
                .findAll(BannerSpecifications.isInBanners(banners).and(BannerSpecifications.fetchPlaceInformation()));
        }
    }
}
