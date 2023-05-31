package com.bannergress.backend.repositories;

import com.bannergress.backend.entities.BannerSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for banner settings.
 */
public interface BannerSettingsRepository extends JpaRepository<BannerSettings, UUID> {
    Optional<BannerSettings> findByUserIdAndBannerCanonicalSlug(String userId, String bannerSlug);

    List<BannerSettings> findByUserIdAndBannerCanonicalSlugIn(String userId, Collection<String> bannerSlugs);
}
