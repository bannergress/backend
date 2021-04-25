package com.bannergress.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class PlaceDto {
    public String id;

    public String formattedAddress;

    public String longName;

    public String shortName;

    public int numberOfBanners;

    public Double boundaryMinLatitude;

    public Double boundaryMinLongitude;

    public Double boundaryMaxLatitude;

    public Double boundaryMaxLongitude;

    public PlaceDto parentPlace;
}
