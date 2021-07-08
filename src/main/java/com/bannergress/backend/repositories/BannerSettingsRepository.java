package com.bannergress.backend.repositories;

import com.bannergress.backend.entities.BannerSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for banner settings.
 */
@Repository
public interface BannerSettingsRepository extends JpaRepository<BannerSettings, UUID> {
    Optional<BannerSettings> findByUserIdAndBannerSlug(String userId, String bannerSlug);

    List<BannerSettings> findByUserIdAndBannerSlugIn(String userId, Collection<String> bannerSlugs);
}
