package com.bannergress.backend.testutils.builder;

import com.bannergress.backend.entities.*;
import com.bannergress.backend.enums.*;

import javax.validation.constraints.NotNull;
import java.util.List;

import static com.bannergress.backend.testutils.builder.BuilderMethods.*;
import static com.bannergress.backend.testutils.builder.JavatypeBuilder.*;

public class EntityBuilder {

    @NotNull
    public static BannerBuilder $Banner() {
        return new BannerBuilder()
            .withId(-1L)
            .withTitle(a($String("title")))
            .withDescription(a($String("description")))
            .withNumberOfMissions(a($Int()))
            .withMissions(sortedMapWith(0, $Mission()))
            .withStartLatitude(a($Double()))
            .withStartLongitude(a($Double()))
            .withLengthMeters(a($Double()))
            .withComplete(a($Boolean()))
            .withOnline(a($Boolean()))
            .withPicture(a($BannerPicture()))
            .withStartPlaces(setWith($Place()));
    }

    @NotNull
    public static MissionBuilder $Mission() {
        return new MissionBuilder()
            .withId(null)
            .withTitle(a($String("title")))
            .withDescription(a($String("description")))
            .withPicture(a($URL()))
            .withAuthor(a($NamedAgent()))
            .withRating(a($Double()))
            .withNumberCompleted(a($Int()))
            .withAverageDurationMilliseconds(a($Long()))
            .withType(oneOf(MissionType.values()))
            .withSteps(List.of())
            .withOnline(a($Boolean()));
    }

    @NotNull
    public static BannerPictureBuilder $BannerPicture() {
        return new BannerPictureBuilder()
            .withHash(a($String("hash")))
            .withPicture(new byte[0]);
    }

    @NotNull
    public static PlaceBuilder $Place() {
        return new PlaceBuilder()
            .withId(a($String("id")))
            .withType(oneOf(PlaceType.values()))
            .withInformation(listWith($PlaceInformation()));
    }

    @NotNull
    public static PlaceInformationBuilder $PlaceInformation() {
        return new PlaceInformationBuilder()
            .withId(a($Long()))
            .withPlace(null)
            .withLanguageCode(a($String("languagecode")))
            .withLongName(a($String("longname")))
            .withShortName(a($String("shortname")))
            .withFormattedAddress(a($String("address")));
    }

    @NotNull
    public static NamedAgentBuilder $NamedAgent() {
        return new NamedAgentBuilder()
            .withName(a($String("name")))
            .withFaction(oneOf(Faction.values()));
    }

    @NotNull
    public static MissionStepBuilder $MissionStep() {
        return new MissionStepBuilder()
            .withId(a($Long()))
            .withMission(a($Mission()))
            .withPoi(a($POI()))
            .withObjective(oneOf(Objective.values()));
    }

    @NotNull
    public static POIBuilder $POI() {
        return new POIBuilder()
            .withId(a($String("id")))
            .withTitle(a($String("title")))
            .withLatitude(a($Double()))
            .withLongitude(a($Double()))
            .withPicture(a($URL()))
            .withType(oneOf(POIType.values()));
    }
}