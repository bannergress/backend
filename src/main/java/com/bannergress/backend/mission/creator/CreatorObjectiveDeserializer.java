package com.bannergress.backend.mission.creator;

import com.bannergress.backend.mission.step.Objective;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

public class CreatorObjectiveDeserializer extends ValueDeserializer<Objective> {
    public Objective deserialize(JsonParser parser, DeserializationContext ctxt) {
        if (parser.hasToken(JsonToken.VALUE_STRING)) {
            String value = parser.getValueAsString();
            switch (value) {
                case "HACK_PORTAL":
                    return Objective.hack;
                case "INSTALL_MOD":
                    return Objective.installMod;
                case "CAPTURE_PORTAL":
                    return Objective.captureOrUpgrade;
                case "CREATE_LINK":
                    return Objective.createLink;
                case "CREATE_FIELD":
                    return Objective.createField;
                case "TAKE_PHOTO":
                    return Objective.takePhoto;
                case "VIEW_FIELD_TRIP_CARD":
                    return Objective.viewWaypoint;
                case "PASSPHRASE":
                    return Objective.enterPassphrase;
                default:
                    return (Objective) ctxt.handleWeirdStringValue(Objective.class, value, "Illegal value");
            }
        } else {
            return (Objective) ctxt.handleUnexpectedToken(Objective.class, parser);
        }
    }
}
