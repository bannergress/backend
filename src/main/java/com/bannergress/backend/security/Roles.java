package com.bannergress.backend.security;

/**
 * Roles definitions.
 */
public interface Roles {
    /**
     * Role which allows to create a banner.
     */
    String CREATE_BANNER = "create-banner";

    /**
     * Role which allows to import data.
     */
    String IMPORT_DATA = "import-data";

    /**
     * Role which allows to manage news items.
     */
    String MANAGE_NEWS = "manage-news";

    /**
     * Role which allows to manage banners.
     */
    String MANAGE_BANNERS = "manage-banners";
}
