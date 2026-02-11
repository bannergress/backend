package com.bannergress.backend.banner.search;

import com.bannergress.backend.banner.Banner;
import com.bannergress.backend.banner.BannerRepository;
import com.bannergress.backend.banner.BannerSpecifications;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Base class for banner search implementations.
 */
abstract class BaseBannerSearchServiceImpl implements BannerSearchService {
    @Autowired
    protected NianticConfiguration nianticConfiguration;

    @Autowired
    protected BannerRepository bannerRepository;

    protected final void preloadPlaceInformation(List<Banner> banners) {
        if (!banners.isEmpty()) {
            bannerRepository
                .findAll(BannerSpecifications.isInBanners(banners).and(BannerSpecifications.fetchPlaceInformation()));
        }
    }
}
