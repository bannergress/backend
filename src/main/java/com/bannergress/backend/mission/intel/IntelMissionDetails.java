package com.bannergress.backend.mission.intel;

import com.bannergress.backend.agent.Faction;
import com.bannergress.backend.mission.MissionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

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
