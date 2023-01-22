package com.bannergress.backend.dto.serialization;

import com.bannergress.backend.enums.MissionType;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class IntelMissionTypeDeserializer extends JsonDeserializer<MissionType> {
    @Override
    public MissionType deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
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
