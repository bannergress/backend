package com.bannergress.backend.event;

import com.bannergress.backend.entities.Banner;

/**
 * Event that fires when a banner is changed.
 */
public class BannerChangedEvent {
    private final Banner banner;

    public BannerChangedEvent(Banner banner) {
        this.banner = banner;
    }

    public Banner getBanner() {
        return banner;
    }
}
