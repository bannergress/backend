package com.bannergress.backend.sitemap;

import com.fasterxml.jackson.annotation.JsonRootName;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

@JsonRootName(namespace = Urlset.NAMESPACE, value = "urlset")
public class Urlset {
    protected static final String NAMESPACE = "http://www.sitemaps.org/schemas/sitemap/0.9";

    @JacksonXmlProperty(namespace = Urlset.NAMESPACE, localName = "url")
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<Url> urls;

    public static class Url {
        @JacksonXmlProperty(namespace = Urlset.NAMESPACE, localName = "loc")
        public String loc;
    }
}
