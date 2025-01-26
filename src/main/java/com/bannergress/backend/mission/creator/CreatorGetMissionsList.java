package com.bannergress.backend.mission.creator;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CreatorGetMissionsList {
    @NotNull
    public List<@NotNull List<com.bannergress.backend.mission.creator.CreatorMission>> missionLists;
}
