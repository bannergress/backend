package com.bannergress.backend.restrictedarea;

public enum RestrictionType {
    /** A fee must be paid to access the restricted area. */
    monetary,
    /** Restricted area has opening hours. */
    temporal,
    /** Only selected people can access the restricted area. */
    audience
}
