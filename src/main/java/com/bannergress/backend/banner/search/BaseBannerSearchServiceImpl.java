package com.bannergress.backend.banner.search;

import com.bannergress.backend.banner.Banner;
import com.bannergress.backend.banner.BannerRepository;
import com.bannergress.backend.banner.BannerSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

/**
 * Base class for banner search implementations.
 */
abstract class BaseBannerSearchServiceImpl implements BannerSearchService {
    @Value("${niantic.officialMissionAuthors:MissionbyNIA,MissionsbyNIA,MissionDaysNia,MissionsNIA,MDNIA2,MDNIA,MDNIA2020,MissionsByNIA22}")
    protected List<String> OFFICIAL_MISSION_AUTHORS;

    @Autowired
    protected BannerRepository bannerRepository;

    protected final void preloadPlaceInformation(List<Banner> banners) {
        if (!banners.isEmpty()) {
            bannerRepository
                .findAll(BannerSpecifications.isInBanners(banners).and(BannerSpecifications.fetchPlaceInformation()));
        }
    }
}
