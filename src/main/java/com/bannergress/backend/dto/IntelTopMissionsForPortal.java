package com.bannergress.backend.dto;

import com.bannergress.backend.validation.NianticId;

import javax.validation.constraints.NotNull;

import java.util.List;

public class IntelTopMissionsForPortal {
    @NotNull
    public Request request;

    @NotNull
    public List<IntelMissionSummary> summaries;

    public static class Request {
        @NotNull
        @NianticId
        public String guid;
    }
}
