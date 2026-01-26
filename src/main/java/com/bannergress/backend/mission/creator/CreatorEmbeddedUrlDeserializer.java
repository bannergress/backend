package com.bannergress.backend.mission.creator;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdScalarDeserializer;
import tools.jackson.databind.exc.MismatchedInputException;

import java.net.URL;

/** Deserializer that supports classic URL strings and also strings with a nested JSON structure represented by {@link NestedUrl}. */
public class CreatorEmbeddedUrlDeserializer extends StdScalarDeserializer<URL> {
    protected CreatorEmbeddedUrlDeserializer() {
        super(URL.class);
    }

    @Override
    public URL deserialize(JsonParser parser, DeserializationContext ctxt) {
        try {
            return parser.readValueAs(URL.class);
        } catch (MismatchedInputException e) {
            String json = parser.readValueAs(String.class);
            NestedUrl nestedUrl = parser.objectReadContext().createParser(json).readValueAs(NestedUrl.class);
            return nestedUrl.photoUrl;
        }
    }

    private static class NestedUrl {
        public URL photoUrl;
    }
}
