package com.bannergress.backend.dto;

import com.bannergress.backend.dto.serialization.IntelMissionDetailsDeserializer;
import com.bannergress.backend.enums.Faction;
import com.bannergress.backend.enums.MissionType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import java.util.List;

@JsonDeserialize(using = IntelMissionDetailsDeserializer.class)
public class IntelMissionDetails extends IntelMissionSummary {
    public String description;

    public String authorName;

    public Faction authorFaction;

    public int numberCompleted;

    @NotNull
    public MissionType type;

    @NotEmpty
    public List<IntelMissionStep> steps;
}
