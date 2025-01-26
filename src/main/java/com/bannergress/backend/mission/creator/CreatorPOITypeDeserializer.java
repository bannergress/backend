package com.bannergress.backend.mission.creator;

import com.bannergress.backend.poi.POIType;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class CreatorPOITypeDeserializer extends JsonDeserializer<POIType> {
    public POIType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = p.getCodec().readTree(p);
        switch (node.asText()) {
            case "PORTAL":
                return POIType.portal;
            case "FIELD_TRIP_CARD":
                return POIType.fieldTripWaypoint;
            default:
                throw new IllegalArgumentException(node.asText());
        }
    }
}
