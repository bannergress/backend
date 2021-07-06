package com.bannergress.backend.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

/**
 * Represents the subset of an RSS feed needed for agent verification.
 */
@JacksonXmlRootElement(localName = "rss")
public class RSS {
    private static final String NAMESPACE_DUBLIN_CORE = "http://purl.org/dc/elements/1.1/";

    @JacksonXmlProperty(localName = "channel")
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<RSS.Channel> channels;

    public static class Channel {
        @JacksonXmlProperty(localName = "item")
        @JacksonXmlElementWrapper(useWrapping = false)
        public List<Item> items;
    }

    public static class Item {
        @JacksonXmlProperty(localName = "creator", namespace = NAMESPACE_DUBLIN_CORE)
        public String creator;

        @JacksonXmlProperty(localName = "description")
        public String description;
    }
}
