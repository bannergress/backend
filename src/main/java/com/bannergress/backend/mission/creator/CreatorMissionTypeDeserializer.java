package com.bannergress.backend.mission.creator;

import com.bannergress.backend.mission.MissionType;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

public class CreatorMissionTypeDeserializer extends ValueDeserializer<MissionType> {
    public MissionType deserialize(JsonParser parser, DeserializationContext ctxt) {
        if (parser.hasToken(JsonToken.VALUE_STRING)) {
            String value = parser.getValueAsString();
            switch (value) {
                case "SEQUENTIAL":
                    return MissionType.sequential;
                case "NON_SEQUENTIAL":
                    return MissionType.anyOrder;
                case "HIDDEN_SEQUENTIAL":
                    return MissionType.hidden;
                default:
                    return (MissionType) ctxt.handleWeirdStringValue(MissionType.class, value, "Illegal value");
            }
        } else {
            return (MissionType) ctxt.handleUnexpectedToken(MissionType.class, parser);
        }
    }
}
