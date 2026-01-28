package com.bannergress.backend.mission.intel;

import com.bannergress.backend.agent.Faction;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

public class IntelFactionDeserializer extends ValueDeserializer<Faction> {
    @Override
    public Faction deserialize(JsonParser parser, DeserializationContext ctxt) {
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
