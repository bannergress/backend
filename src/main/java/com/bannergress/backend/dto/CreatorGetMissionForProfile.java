package com.bannergress.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Represents the request and response of a single getMissionForProfile call in the mission creator.
 */
public class CreatorGetMissionForProfile {
    @NotNull
    public Request request;

    @NotNull
    public Response response;

    public static class Request {
        @NotNull
        public String mission_guid;
    }

    public static class Response {
        public Error mat_error;

        public CreatorMission mission;

        public List<@NotNull CreatorPoi> pois;
    }

    public static class Error {
        public ErrorTitle title;
    }

    public enum ErrorTitle {
        @JsonProperty("Mission Not Found")
        missionNotFound
    }
}
