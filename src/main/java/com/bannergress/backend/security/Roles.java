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
}
