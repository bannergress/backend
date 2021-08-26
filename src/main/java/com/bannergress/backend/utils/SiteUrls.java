package com.bannergress.backend.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Provides methods for resolving site URLs.
 */
@Component
public class SiteUrls {
    @Value("${site.prefix.frontend:}")
    private String frontendPrefix;

    @Value("${site.prefix.backend:}")
    private String backendPrefix;

    public String getBannerUrl(String slug) {
        return frontendPrefix + "/banner/" + URLEncoder.encode(slug, StandardCharsets.UTF_8);
    }

    public String getPlaceUrl(String slug) {
        return frontendPrefix + "/browse/" + URLEncoder.encode(slug, StandardCharsets.UTF_8);
    }

    public String getPictureUrl(String hash) {
        return backendPrefix + "/bnrs/pictures/" + URLEncoder.encode(hash, StandardCharsets.UTF_8);
    }
}
