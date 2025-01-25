package com.bannergress.backend.mission.intel;

import com.bannergress.backend.agent.Faction;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class IntelFactionDeserializer extends JsonDeserializer<Faction> {
    @Override
    public Faction deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        if (parser.hasToken(JsonToken.VALUE_STRING)) {
            String value = parser.getValueAsString();
            switch (value) {
                case "E":
                    return Faction.enlightened;
                case "R":
                    return Faction.resistance;
                case "N":
                    return null;
                default:
                    return (Faction) ctxt.handleWeirdStringValue(Faction.class, value, "Illegal value");
            }
        } else {
            return (Faction) ctxt.handleUnexpectedToken(Faction.class, parser);
        }
    }
}
