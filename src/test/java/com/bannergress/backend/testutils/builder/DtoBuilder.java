package com.bannergress.backend.testutils.builder;


import com.bannergress.backend.dto.BannerDtoBuilder;
import com.bannergress.backend.dto.MissionDtoBuilder;
import com.bannergress.backend.dto.MissionStepDtoBuilder;
import com.bannergress.backend.dto.PoiDtoBuilder;
import com.bannergress.backend.enums.Objective;
import com.bannergress.backend.enums.POIType;

import javax.validation.constraints.NotNull;

import java.util.Collections;

import static com.bannergress.backend.testutils.builder.BuilderMethods.a;
import static com.bannergress.backend.testutils.builder.BuilderMethods.listWith;
import static com.bannergress.backend.testutils.builder.BuilderMethods.oneOf;
import static com.bannergress.backend.testutils.builder.JavatypeBuilder.*;

public class DtoBuilder {


    @NotNull
    public static BannerDtoBuilder $BannerDto() {
        return new BannerDtoBuilder()
            .withUuid(a($UUID()))
            .withTitle(a($String("title")))
            .withDescription(a($String("description")))
            .withNumberOfMissions(a($Int()))
            .withMissions(Collections.singletonMap(0, a($MissionDto())))
            .withStartLatitude(a($Double()))
            .withStartLongitude(a($Double()))
            .withLengthMeters(a($Double()))
            .withFormattedAddress(a($String("address")));
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
