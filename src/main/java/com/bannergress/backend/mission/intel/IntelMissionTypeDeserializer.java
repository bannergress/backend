package com.bannergress.backend.mission.intel;

import com.bannergress.backend.mission.MissionType;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

public class IntelMissionTypeDeserializer extends ValueDeserializer<MissionType> {
    @Override
    public MissionType deserialize(JsonParser parser, DeserializationContext ctxt) {
        if (parser.hasToken(JsonToken.VALUE_NUMBER_INT)) {
            int value = parser.getValueAsInt();
            switch (value) {
                case 1:
                    return MissionType.sequential;
                case 2:
                    return MissionType.anyOrder;
                case 3:
                    return MissionType.hidden;
                default:
                    return (MissionType) ctxt.handleWeirdNumberValue(MissionType.class, value, "Illegal value");
            }
        } else {
            return (MissionType) ctxt.handleUnexpectedToken(MissionType.class, parser);
        }
    }
}
