package com.bannergress.backend.banner;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

/** Represents the subset of GPX used for exporting banners. */
@JacksonXmlRootElement(localName = "gpx", namespace = Gpx.NAMESPACE)
public class Gpx {
    /** XML namespace. */
    static final String NAMESPACE = "http://www.topografix.com/GPX/1/1";

    /** De facto mime type (though not officially registered with IANA). */
    public static final String MIMETYPE = "application/gpx+xml";

    /** File suffix. */
    public static final String SUFFIX = ".gpx";

    @JacksonXmlProperty(isAttribute = true)
    public final String version = "1.1";

    @JacksonXmlProperty(isAttribute = true)
    public final String creator = "Bannergress";

    @JacksonXmlProperty(namespace = NAMESPACE)
    public Metadata metadata;

    @JacksonXmlProperty(namespace = NAMESPACE)
    public Route rte;

    public static class Metadata {
        @JacksonXmlProperty(namespace = NAMESPACE)
        public String name;
    }

    public static class Route {
        @JacksonXmlProperty(namespace = NAMESPACE)
        public String name;

        @JacksonXmlProperty(namespace = NAMESPACE)
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<Waypoint> rtept;
    }

    public static class Waypoint {
        @JacksonXmlProperty(isAttribute = true)
        public double lat;

        @JacksonXmlProperty(isAttribute = true)
        public double lon;

        @JacksonXmlProperty(namespace = NAMESPACE)
        public String name;
    }
}
