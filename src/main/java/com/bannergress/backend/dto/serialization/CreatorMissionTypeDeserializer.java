package com.bannergress.backend.dto.serialization;

import com.bannergress.backend.enums.MissionType;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class CreatorMissionTypeDeserializer extends JsonDeserializer<MissionType> {
    public MissionType deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException, JsonProcessingException {
        JsonNode node = p.getCodec().readTree(p);
        switch (node.asText()) {
            case "SEQUENTIAL":
                return MissionType.sequential;
            case "NON_SEQUENTIAL":
                return MissionType.anyOrder;
            case "HIDDEN_SEQUENTIAL":
                return MissionType.hidden;
            default:
                throw new IllegalArgumentException(node.asText());
        }
    }
}
