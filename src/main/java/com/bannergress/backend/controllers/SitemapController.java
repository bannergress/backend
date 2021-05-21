package com.bannergress.backend.controllers;

import com.bannergress.backend.dto.Urlset;
import com.bannergress.backend.services.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST endpoint for sitemap.
 */
@RestController
public class SitemapController {
    @Autowired
    BannerService bannerService;

    @Value("${sitemap.prefix.banner:}")
    private String bannerUrlPrefix;

    @GetMapping(value = "/sitemap/banners", produces = MediaType.APPLICATION_XML_VALUE)
    public Urlset getBannerSitemap() {
        List<String> slugs = bannerService.findAllSlugs();
        return toUrlset(slugs);
    }

    private Urlset toUrlset(List<String> slugs) {
        Urlset result = new Urlset();
        result.urls = slugs.stream().map(slug -> {
            Urlset.Url url = new Urlset.Url();
            url.loc = bannerUrlPrefix + URLEncoder.encode(slug, StandardCharsets.UTF_8);
            return url;
        }).collect(Collectors.toList());
        return result;
    }
}
