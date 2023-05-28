package com.bannergress.backend.dto;

import com.bannergress.backend.validation.NianticId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.net.URL;

/**
 * Representation of a mission summary from the Ingress intel API.
 */
@JsonFormat(shape = Shape.ARRAY)
@JsonPropertyOrder({"id", "title", "picture", "ratingE6", "averageDurationMilliseconds"})
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
