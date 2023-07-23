package com.bannergress.backend.dto.serialization;

import com.bannergress.backend.dto.IntelMissionStep;
import com.bannergress.backend.enums.Objective;
import com.bannergress.backend.enums.POIType;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.net.URL;

public class IntelMissionStepDeserializer extends JsonDeserializer<IntelMissionStep> {
    @Override
    public IntelMissionStep deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        ArrayNode node = parser.readValueAsTree();
        IntelMissionStep result = new IntelMissionStep();
        result.hidden = ctxt.readTreeAsValue(node.get(0), boolean.class);
        result.id = ctxt.readTreeAsValue(node.get(1), String.class);
        if (!result.hidden) {
            result.objective = readObjective(ctxt, node.get(4));
            if (node.get(5).isNull()) {
                result.type = POIType.unavailable;
            } else {
                result.title = ctxt.readTreeAsValue(node.get(2), String.class);
                result.type = readPOIType(ctxt, node.get(3));
                ArrayNode poiNode = (ArrayNode) node.get(5);
                switch (result.type) {
                    case portal:
                        result.latitudeE6 = ctxt.readTreeAsValue(poiNode.get(2), int.class);
                        result.longitudeE6 = ctxt.readTreeAsValue(poiNode.get(3), int.class);
                        JsonNode pictureNode = poiNode.get(7);
                        result.picture = pictureNode.isNull() ? null : ctxt.readTreeAsValue(pictureNode, URL.class);
                        break;
                    case fieldTripWaypoint:
                        result.latitudeE6 = ctxt.readTreeAsValue(poiNode.get(1), int.class);
                        result.longitudeE6 = ctxt.readTreeAsValue(poiNode.get(2), int.class);
                        break;
                    default:
                        break;
                }
            }
        }
        return result;
    }

    private Objective readObjective(DeserializationContext ctxt, JsonNode node) throws IOException {
        int value = ctxt.readTreeAsValue(node, int.class);
        switch (value) {
            case 1:
                return Objective.hack;
            case 2:
                return Objective.captureOrUpgrade;
            case 3:
                return Objective.createLink;
            case 4:
                return Objective.createField;
            case 5:
                return Objective.installMod;
            case 6:
                return Objective.takePhoto;
            case 7:
                return Objective.viewWaypoint;
            case 8:
                return Objective.enterPassphrase;
            default:
                return (Objective) ctxt.handleWeirdNumberValue(Objective.class, value, "Illegal value");
        }
    }

    private POIType readPOIType(DeserializationContext ctxt, JsonNode node) throws IOException {
        int value = ctxt.readTreeAsValue(node, int.class);
        switch (value) {
            case 1:
                return POIType.portal;
            case 2:
                return POIType.fieldTripWaypoint;
            default:
                return (POIType) ctxt.handleWeirdNumberValue(POIType.class, value, "Illegal value");
        }
    }
}
