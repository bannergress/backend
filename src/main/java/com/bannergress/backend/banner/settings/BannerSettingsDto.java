package com.bannergress.backend.banner.settings;

import com.bannergress.backend.banner.BannerListType;
import jakarta.validation.constraints.NotNull;

public class BannerSettingsDto {
    @NotNull
    /**
     * Type of list the banner is on.
     */
    public BannerListType listType;
}
