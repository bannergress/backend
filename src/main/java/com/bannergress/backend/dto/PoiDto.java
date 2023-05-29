package com.bannergress.backend.dto;

import com.bannergress.backend.enums.POIType;
import com.bannergress.backend.utils.PojoBuilder;
import com.bannergress.backend.validation.NianticId;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import net.karneim.pojobuilder.GeneratePojoBuilder;

import java.net.URL;

@JsonInclude(Include.NON_NULL)
@GeneratePojoBuilder(withBuilderInterface = PojoBuilder.class)
public class PoiDto {
    /**
     * Ingress POI ID.
     */
    @NianticId
    public String id;

    /**
     * Title.
     */
    @NotEmpty
    public String title;

    /**
     * Latitude.
     */
    @Min(-90)
    @Max(90)
    public Double latitude;

    /**
     * Longitude.
     */
    @Min(-180)
    @Max(180)
    public Double longitude;

    /**
     * Picture URL.
     */
    @NotNull
    public URL picture;

    /**
     * Type of the POI (portal or field trip waypoint.
     */
    @NotNull
    public POIType type;
}
