package com.bannergress.backend.controllers;

import com.bannergress.backend.entities.Banner;
import com.bannergress.backend.entities.PlaceInformation;
import com.bannergress.backend.services.BannerService;
import com.bannergress.backend.services.PlaceService;
import com.bannergress.backend.utils.SiteUrls;
import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Optional;

/**
 * Controller for HTML meta tags.
 */
@RestController
public class MetaController {
    private static final String OG_DESCRIPTION = "og:description";
    private static final String OG_IMAGE = "og:image";
    private static final String OG_SITE_NAME = "og:site_name";
    private static final String OG_SITE_NAME_BANNERGRESS = "Bannergress";
    private static final String OG_TITLE = "og:title";
    private static final String OG_TYPE = "og:type";
    private static final String OG_TYPE_ARTICLE = "article";
    private static final String OG_URL = "og:url";
    private static final String TWITTER_CARD = "twitter:card";
    private static final String TWITTER_CARD_LARGE = "summary_large_image";
    private static final String UNKNOWN_DISTANCE = "unknown distance";
    private static final String UNKNOWN_LOCATION = "unknown location";

    @Autowired
    private BannerService bannerService;

    @Autowired
    private PlaceService placeService;

    @Autowired
    private SiteUrls siteUrls;

    @GetMapping("/meta/banner/{id}")
    public String getBannerMeta(@PathVariable String id) {
        Optional<Banner> optionalBanner = bannerService.findBySlugWithDetails(id);
        if (optionalBanner.isPresent()) {
            Banner banner = optionalBanner.get();
            String title = banner.getTitle();
            String distance = getDistance(banner);
            String place = getPlaceName(banner);
            String description = String.format("%s Missions, %s\n%s", banner.getNumberOfMissions(), distance, place);
            String url = siteUrls.getBannerUrl(banner.getCanonicalSlug());
            String pictureUrl = banner.getPicture() == null ? null
                : siteUrls.getPictureUrl(banner.getPicture().getHash());
            return new MetaBuilder() //
                .add(OG_SITE_NAME, OG_SITE_NAME_BANNERGRESS) //
                .add(OG_TYPE, OG_TYPE_ARTICLE) //
                .add(TWITTER_CARD, TWITTER_CARD_LARGE) //
                .add(OG_TITLE, title) //
                .add(OG_DESCRIPTION, description)//
                .add(OG_URL, url) //
                .add(OG_IMAGE, pictureUrl) //
                .toString();
        } else {
            return "";
        }
    }

    private String getPlaceName(Banner banner) {
        Optional<PlaceInformation> placeInformation = placeService
            .getMostAccuratePlaceInformation(banner.getStartPlaces(), "en");
        return placeInformation.map(PlaceInformation::getFormattedAddress).orElse(UNKNOWN_LOCATION);
    }

    private String getDistance(Banner banner) {
        return Optional.ofNullable(banner.getLengthMeters()).map(distance -> {
            if (distance >= 995) {
                return BigDecimal.valueOf(distance / 1000d).round(new MathContext(2)).toPlainString() + "\u00A0km";
            } else {
                return BigDecimal.valueOf(distance).round(new MathContext(2)).toPlainString() + "\u00A0m";
            }
        }).orElse(UNKNOWN_DISTANCE);
    }

    @GetMapping("/meta/**")
    public String getDefaultMeta() {
        return "";
    }

    private static class MetaBuilder {
        private final Escaper escaper = HtmlEscapers.htmlEscaper();
        private final StringBuilder builder = new StringBuilder();

        public MetaBuilder add(String key, String value) {
            if (value != null) {
                builder.append("<meta property=\"").append(escaper.escape(key)).append("\" content=\"")
                    .append(escaper.escape(value)).append("\">");
            }
            return this;
        }

        @Override
        public String toString() {
            return builder.toString();
        }
    }
}
