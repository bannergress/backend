package com.bannergress.backend.testutils.builder;


import com.bannergress.backend.banner.BannerDtoBuilder;
import com.bannergress.backend.banner.BannerType;
import com.bannergress.backend.mission.MissionDtoBuilder;
import com.bannergress.backend.mission.step.MissionStepDtoBuilder;
import com.bannergress.backend.mission.step.Objective;
import com.bannergress.backend.poi.POIType;
import com.bannergress.backend.poi.PoiDtoBuilder;
import jakarta.validation.constraints.NotNull;

import java.util.Collections;

import static com.bannergress.backend.testutils.builder.BuilderMethods.a;
import static com.bannergress.backend.testutils.builder.BuilderMethods.listWith;
import static com.bannergress.backend.testutils.builder.BuilderMethods.oneOf;
import static com.bannergress.backend.testutils.builder.JavatypeBuilder.$Double;
import static com.bannergress.backend.testutils.builder.JavatypeBuilder.$Int;
import static com.bannergress.backend.testutils.builder.JavatypeBuilder.$String;
import static com.bannergress.backend.testutils.builder.JavatypeBuilder.$URL;

public class DtoBuilder {


    @NotNull
    public static BannerDtoBuilder $BannerDto() {
        return new BannerDtoBuilder()
            .withId(a($String()))
            .withTitle(a($String("title")))
            .withDescription(a($String("description")))
            .withWidth(6)
            .withNumberOfMissions(a($Int()))
            .withMissions(Collections.singletonMap(0, a($MissionDto())))
            .withStartLatitude(a($Double()))
            .withStartLongitude(a($Double()))
            .withLengthMeters(a($Int()))
            .withFormattedAddress(a($String("address")))
            .withType(BannerType.sequential);
    }

    @NotNull
    public static MissionDtoBuilder $MissionDto() {
        return new MissionDtoBuilder()
            .withId(a($String("id")))
            .withTitle(a($String("title")))
            .withPicture(a($URL()))
            .withSteps(listWith($MissionStepDto()));
    }

    @NotNull
    public static MissionStepDtoBuilder $MissionStepDto() {
        return new MissionStepDtoBuilder()
            .withPoi(a($PoiDto()))
            .withObjective(oneOf(Objective.values()));
    }

    @NotNull
    public static PoiDtoBuilder $PoiDto() {
        return new PoiDtoBuilder()
            .withId(a($String("id")))
            .withTitle(a($String("title")))
            .withLatitude(a($Double()))
            .withLongitude(a($Double()))
            .withPicture(a($URL()))
            .withType(oneOf(POIType.values()));
    }
}
