package com.bannergress.backend.controllers;

import com.bannergress.backend.dto.Urlset;
import com.bannergress.backend.services.BannerService;
import com.bannergress.backend.utils.SiteUrls;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST endpoint for sitemap.
 */
@RestController
public class SitemapController {
    @Autowired
    BannerService bannerService;

    @Autowired
    private SiteUrls siteUrls;

    @GetMapping(value = "/sitemap/banners", produces = MediaType.APPLICATION_XML_VALUE)
    public Urlset getBannerSitemap() {
        List<String> slugs = bannerService.findAllSlugs();
        return toUrlset(slugs);
    }

    private Urlset toUrlset(List<String> slugs) {
        Urlset result = new Urlset();
        result.urls = slugs.stream().map(slug -> {
            Urlset.Url url = new Urlset.Url();
            url.loc = siteUrls.getBannerUrl(slug);
            return url;
        }).collect(Collectors.toList());
        return result;
    }
}
