package com.bannergress.backend.dto.serialization;

import com.bannergress.backend.enums.Objective;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class CreatorObjectiveDeserializer extends JsonDeserializer<Objective> {
    public Objective deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException, JsonProcessingException {
        JsonNode node = p.getCodec().readTree(p);
        switch (node.asText()) {
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
                throw new IllegalArgumentException(node.asText());
        }
    }
}
