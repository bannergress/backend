package com.bannergress.backend.banner.settings;

import com.bannergress.backend.banner.BannerListType;
import com.bannergress.backend.banner.BannerRepository;
import com.bannergress.backend.user.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BannerSettingsServiceImpl implements BannerSettingsService {
    @Autowired
    private BannerRepository bannerRepository;

    @Autowired
    private BannerSettingsRepository bannerSettingsRepository;

    @Autowired
    private UserService userService;

    @Override
    public void addBannerToList(String userId, String bannerSlug, BannerListType listType) {
        BannerSettings settings = getOrCreate(userId, bannerSlug);
        settings.setListType(listType);
        settings.setListAdded(Instant.now());
    }

    @Override
    public List<BannerSettings> getBannerSettings(String userId, Collection<String> bannerSlugs) {
        return bannerSettingsRepository.findByUserIdAndBannerCanonicalSlugIn(userId, bannerSlugs);
    }

    private BannerSettings getOrCreate(String userId, String bannerSlug) {
        Optional<BannerSettings> optionalBannerSettings = bannerSettingsRepository
            .findByUserIdAndBannerCanonicalSlug(userId, bannerSlug);
        return optionalBannerSettings.orElseGet(() -> {
            BannerSettings bannerSettings = new BannerSettings();
            bannerSettings.setUser(userService.getOrCreate(userId));
            bannerSettings.setBanner(bannerRepository.findByCanonicalSlug(bannerSlug).get());
            return bannerSettingsRepository.save(bannerSettings);
        });
    }
}
