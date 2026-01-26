package com.bannergress.backend.mission.creator;

import com.bannergress.backend.poi.POIType;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

public class CreatorPOITypeDeserializer extends ValueDeserializer<POIType> {
    public POIType deserialize(JsonParser parser, DeserializationContext ctxt) {
        if (parser.hasToken(JsonToken.VALUE_STRING)) {
            String value = parser.getValueAsString();
            switch (value) {
                case "PORTAL":
                    return POIType.portal;
                case "FIELD_TRIP_CARD":
                    return POIType.fieldTripWaypoint;
                default:
                    return (POIType) ctxt.handleWeirdStringValue(POIType.class, value, "Illegal value");
            }
        } else {
            return (POIType) ctxt.handleUnexpectedToken(POIType.class, parser);
        }
    }
}
