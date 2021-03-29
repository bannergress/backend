package com.bannergress.backend.dto.serialization;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.bannergress.backend.dto.IntelMissionDetails;
import com.bannergress.backend.dto.IntelMissionStep;
import com.bannergress.backend.enums.Faction;
import com.bannergress.backend.enums.MissionType;
import com.bannergress.backend.enums.Objective;
import com.bannergress.backend.enums.POIType;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class IntelMissionDetailsDeserializer extends JsonDeserializer<IntelMissionDetails> {
	@Override
	public IntelMissionDetails deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		IntelMissionDetails result = new IntelMissionDetails();
		ArrayNode array = p.getCodec().readTree(p);
		result.id = array.get(0).textValue();
		result.title = array.get(1).textValue();
		result.description = array.get(2).textValue();
		result.authorName = array.get(3).textValue();
		result.authorFaction = parseFaction(array.get(4).textValue());
		result.ratingE6 = array.get(5).asInt();
		result.averageDurationMilliseconds = array.get(6).asLong();
		result.numberCompleted = array.get(7).asInt();
		result.type = parseMissionType(array.get(8).asInt());
		result.steps = deserializeSteps((ArrayNode) array.get(9));
		result.picture = new URL(array.get(10).textValue());
		return result;
	}

	private List<IntelMissionStep> deserializeSteps(ArrayNode arrayNode) throws MalformedURLException {
		List<IntelMissionStep> result = new ArrayList<>(arrayNode.size());
		for (JsonNode node : arrayNode) {
			IntelMissionStep step = new IntelMissionStep();
			step.hidden = node.get(0).booleanValue();
			step.id = node.get(1).textValue();
			step.objective = parseObjective(node.get(4).intValue());
			if (node.get(5).isNull()) {
				step.type = POIType.unavailable;
			} else {
				step.title = node.get(2).textValue();
				step.type = parsePOIType(node.get(3).intValue());
				ArrayNode poiNode = (ArrayNode) node.get(5);
				step.latitudeE6 = poiNode.get(2).asInt();
				step.longitudeE6 = poiNode.get(3).asInt();
				step.picture = new URL(poiNode.get(7).textValue());
			}
			result.add(step);
		}
		return result;
	}

	private Faction parseFaction(String faction) {
		switch (faction) {
		case "E":
			return Faction.enlightened;
		case "R":
			return Faction.resistance;
		default:
			throw new IllegalArgumentException();
		}
	}

	private Objective parseObjective(int objective) {
		switch (objective) {
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
			throw new IllegalArgumentException();
		}
	}

	private MissionType parseMissionType(int type) {
		switch (type) {
		case 1:
			return MissionType.sequential;
		case 2:
			return MissionType.anyOrder;
		case 3:
			return MissionType.hidden;
		default:
			throw new IllegalArgumentException();
		}
	}

	private POIType parsePOIType(int type) {
		switch (type) {
		case 1:
			return POIType.portal;
		case 2:
			return POIType.fieldTripWaypoint;
		default:
			throw new IllegalArgumentException();
		}
	}
}
