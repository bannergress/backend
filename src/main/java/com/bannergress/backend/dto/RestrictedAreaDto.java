package com.bannergress.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.locationtech.jts.geom.Geometry;

import java.net.URL;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(Include.NON_NULL)
public class RestrictedAreaDto {
    public UUID id;

    public String title;

    public Geometry area;

    public Restriction generalRestriction;

    public Restriction timedRestriction;

    public Restriction monetaryRestriction;

    public Instant startDate;

    public Instant endDate;

    public static class Restriction {
        public String description;

        public URL url;
    }
}
