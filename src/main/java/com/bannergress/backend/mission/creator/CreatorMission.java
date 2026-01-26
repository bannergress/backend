package com.bannergress.backend.mission.creator;

import com.bannergress.backend.mission.MissionType;
import com.bannergress.backend.mission.step.Objective;
import com.bannergress.backend.mission.validation.NianticId;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.net.URL;
import java.util.List;

public class CreatorMission {
    @NotNull
    public Definition definition;

    @NianticId
    public String mission_guid;

    public CreatorMissionStatus state;

    @NotNull
    public Stats stats;

    public static class Stats {
        @Min(0)
        public long median_completion_time;

        @Min(0)
        public int num_completed;

        @Min(0)
        @Max(100)
        public int rating;
    }

    public static class Definition {
        public String author_nickname;

        public String description;

        @NianticId
        public String guid;

        public URL logo_url;

        @JsonDeserialize(using = CreatorMissionTypeDeserializer.class)
        public MissionType mission_type;

        public String name;

        public List<Waypoint> waypoints;
    }

    public static class Waypoint {
        public boolean hidden;

        public ObjectiveDetails objective;

        @NianticId
        public String poi_guid;
    }

    public static class ObjectiveDetails {
        @JsonDeserialize(using = CreatorObjectiveDeserializer.class)
        public Objective type;
    }
}
