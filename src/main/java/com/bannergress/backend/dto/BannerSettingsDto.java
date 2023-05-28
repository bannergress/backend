package com.bannergress.backend.dto;

import com.bannergress.backend.enums.BannerListType;
import jakarta.validation.constraints.NotNull;

public class BannerSettingsDto {
    @NotNull
    /**
     * Type of list the banner is on.
     */
    public BannerListType listType;
}
