package com.bannergress.backend.dto;

import com.bannergress.backend.dto.serialization.CreatorPOITypeDeserializer;
import com.bannergress.backend.enums.POIType;
import com.bannergress.backend.validation.NianticId;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import java.net.URL;

public class CreatorPoi {
    @NianticId
    public String guid;

    @NotNull
    public URL imageUrl;

    @NotNull
    public Location location;

    @NotNull
    public String title;

    @NotNull
    @JsonDeserialize(using = CreatorPOITypeDeserializer.class)
    public POIType type;

    public static class Location {
        @Min(-90)
        @Max(90)
        public double latitude;

        @Min(-180)
        @Max(180)
        public double longitude;
    }
}
