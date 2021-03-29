package com.bannergress.backend.dto.serialization;

import java.io.IOException;
import java.net.URL;

import com.bannergress.backend.dto.IntelMissionSummary;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class IntelMissionSummaryDeserializer extends JsonDeserializer<IntelMissionSummary> {
	@Override
	public IntelMissionSummary deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		IntelMissionSummary result = new IntelMissionSummary();
		ArrayNode array = p.getCodec().readTree(p);
		result.id = array.get(0).textValue();
		result.title = array.get(1).textValue();
		result.picture = new URL(array.get(2).textValue());
		result.ratingE6 = array.get(3).asInt();
		result.averageDurationMilliseconds = array.get(4).asLong();
		return result;
	}
}
