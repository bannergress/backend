package com.bannergress.backend.dto;

import com.bannergress.backend.dto.serialization.IntelMissionSummaryDeserializer;
import com.bannergress.backend.validation.NianticId;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import java.net.URL;

/**
 * Representation of a mission summary from the Ingress intel API.
 */
@JsonDeserialize(using = IntelMissionSummaryDeserializer.class)
public class IntelMissionSummary {
    @NianticId
    @NotNull
    public String id;

    @NotEmpty
    public String title;

    @NotNull
    public URL picture;

    @Min(0)
    @Max(1_000_000)
    public int ratingE6;

    @Min(0)
    public long averageDurationMilliseconds;
}
