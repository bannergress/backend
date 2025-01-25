package com.bannergress.backend.banner;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Possible types of banner lists.
 */
@Schema(enumAsRef = true)
public enum BannerListType {
    /**
     * No list.
     */
    none,
    /**
     * Todo list.
     */
    todo,
    /**
     * Done list.
     */
    done,
    /**
     * Blacklist.
     */
    blacklist
}
