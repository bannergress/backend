package com.bannergress.backend.sitemap;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(namespace = Urlset.NAMESPACE, localName = "urlset")
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
