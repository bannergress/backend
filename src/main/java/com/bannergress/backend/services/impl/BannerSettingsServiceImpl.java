package com.bannergress.backend.services.impl;

import com.bannergress.backend.entities.BannerSettings;
import com.bannergress.backend.enums.BannerListType;
import com.bannergress.backend.repositories.BannerRepository;
import com.bannergress.backend.repositories.BannerSettingsRepository;
import com.bannergress.backend.services.BannerSettingsService;
import com.bannergress.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

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
        return bannerSettingsRepository.findByUserIdAndBannerSlugIn(userId, bannerSlugs);
    }

    private BannerSettings getOrCreate(String userId, String bannerSlug) {
        Optional<BannerSettings> optionalBannerSettings = bannerSettingsRepository.findByUserIdAndBannerSlug(userId,
            bannerSlug);
        return optionalBannerSettings.orElseGet(() -> {
            BannerSettings bannerSettings = new BannerSettings();
            bannerSettings.setUser(userService.getOrCreate(userId));
            bannerSettings.setBanner(bannerRepository.findBySlug(bannerSlug).get());
            return bannerSettingsRepository.save(bannerSettings);
        });
    }
}
