package com.bannergress.backend.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

public class IntelTopMissionsInBounds {
    @NotNull
    public Request request;

    @NotNull
    public List<IntelMissionSummary> summaries;

    public static class Request {
        @Min(-90_000_000)
        @Max(90_000_000)
        public int northE6;

        @Min(-90_000_000)
        @Max(90_000_000)
        public int southE6;

        @Min(-180_000_000)
        @Max(180_000_000)
        public int westE6;

        @Min(-180_000_000)
        @Max(180_000_000)
        public int eastE6;
    }
}
