package com.bannergress.backend.banner;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for banners.
 */
public interface BannerRepository extends JpaRepository<Banner, UUID>, JpaSpecificationExecutor<Banner> {
    @Query("SELECT b.uuid FROM Banner b")
    List<UUID> getAllUUIDs();

    @Query("SELECT b.canonicalSlug FROM Banner b")
    List<String> getAllSlugs();

    Optional<Banner> findByCanonicalSlug(String slug);
}
