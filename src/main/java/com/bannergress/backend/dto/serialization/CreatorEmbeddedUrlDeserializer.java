package com.bannergress.backend.dto.serialization;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

import java.io.IOException;
import java.net.URL;

/** Deserializer that supports classic URL strings and also strings with a nested JSON structure represented by {@link NestedUrl}. */
public class CreatorEmbeddedUrlDeserializer extends StdScalarDeserializer<URL> {
    private static final long serialVersionUID = 1L;

    protected CreatorEmbeddedUrlDeserializer() {
        super(URL.class);
    }

    @Override
    public URL deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException, JacksonException {
        try {
            return parser.readValueAs(URL.class);
        } catch (JacksonException e) {
            String json = parser.readValueAs(String.class);
            NestedUrl nestedUrl = ((ObjectMapper) parser.getCodec()).readValue(json, NestedUrl.class);
            return nestedUrl.photoUrl;
        }
    }

    private static class NestedUrl {
        public URL photoUrl;
    }
}
