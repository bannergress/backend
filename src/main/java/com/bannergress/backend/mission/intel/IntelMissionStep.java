package com.bannergress.backend.mission.intel;

import com.bannergress.backend.mission.step.Objective;
import com.bannergress.backend.mission.validation.NianticId;
import com.bannergress.backend.poi.POIType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.net.URL;

public class IntelMissionStep {
    public boolean hidden;

    @NianticId
    public String id;

    public String title;

    public POIType type;

    public Objective objective;

    @Min(-90_000_000)
    @Max(90_000_000)
    public Integer latitudeE6;

    @Min(-180_000_000)
    @Max(180_000_000)
    public Integer longitudeE6;

    public URL picture;
}
