package com.bannergress.backend.dto;

import com.bannergress.backend.dto.serialization.IntelFactionDeserializer;
import com.bannergress.backend.dto.serialization.IntelMissionStepDeserializer;
import com.bannergress.backend.dto.serialization.IntelMissionTypeDeserializer;
import com.bannergress.backend.enums.Faction;
import com.bannergress.backend.enums.MissionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import java.util.List;

@JsonFormat(shape = Shape.ARRAY)
@JsonPropertyOrder({"id", "title", "description", "authorName", "authorFaction", "ratingE6",
    "averageDurationMilliseconds", "numberCompleted", "type", "steps", "picture"})
public class IntelMissionDetails extends IntelMissionSummary {
    public String description;

    public String authorName;

    @JsonDeserialize(using = IntelFactionDeserializer.class)
    public Faction authorFaction;

    public int numberCompleted;

    @NotNull
    @JsonDeserialize(using = IntelMissionTypeDeserializer.class)
    public MissionType type;

    @NotEmpty
    @JsonDeserialize(contentUsing = IntelMissionStepDeserializer.class)
    public List<IntelMissionStep> steps;
}
