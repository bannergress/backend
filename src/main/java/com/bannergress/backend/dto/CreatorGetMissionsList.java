package com.bannergress.backend.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CreatorGetMissionsList {
    @NotNull
    public List<@NotNull List<@NotNull CreatorMission>> missionLists;
}
