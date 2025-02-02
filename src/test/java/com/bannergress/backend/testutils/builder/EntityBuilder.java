package com.bannergress.backend.testutils.builder;

import com.bannergress.backend.agent.Faction;
import com.bannergress.backend.agent.NamedAgentBuilder;
import com.bannergress.backend.banner.BannerBuilder;
import com.bannergress.backend.banner.BannerType;
import com.bannergress.backend.banner.picture.BannerPictureBuilder;
import com.bannergress.backend.mission.MissionBuilder;
import com.bannergress.backend.mission.MissionStatus;
import com.bannergress.backend.mission.MissionType;
import com.bannergress.backend.mission.step.MissionStepBuilder;
import com.bannergress.backend.mission.step.Objective;
import com.bannergress.backend.news.NewsItemBuilder;
import com.bannergress.backend.place.PlaceBuilder;
import com.bannergress.backend.place.PlaceInformationBuilder;
import com.bannergress.backend.place.PlaceType;
import com.bannergress.backend.poi.POIBuilder;
import com.bannergress.backend.poi.POIType;
import jakarta.validation.constraints.NotNull;

import java.util.List;

import static com.bannergress.backend.testutils.builder.BuilderMethods.*;
import static com.bannergress.backend.testutils.builder.JavatypeBuilder.*;

public class EntityBuilder {

    @NotNull
    public static BannerBuilder $Banner() {
        return new BannerBuilder()
            .withUuid(a($UUID()))
            .withTitle(a($String("title")))
            .withDescription(a($String("description")))
            .withWidth(6)
            .withNumberOfMissions(a($Int()))
            .withMissions(sortedMapWith(0, $Mission()))
            .withLengthMeters(a($Int()))
            .withOnline(a($Boolean()))
            .withPicture(a($BannerPicture()))
            .withStartPlaces(setWith($Place()))
            .withCreated(a($Instant()))
            .withType(BannerType.sequential);
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
            .withStatus(MissionStatus.published)
            .withLatestUpdateSummary(a($Instant()))
            .withLatestUpdateDetails(a($Instant()));
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
            .withUuid(a($UUID()))
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
            .withUuid(a($UUID()))
            .withMission(a($Mission()))
            .withPoi(a($POI()))
            .withObjective(oneOf(Objective.values()));
    }

    @NotNull
    public static POIBuilder $POI() {
        return new POIBuilder()
            .withId(a($String("id")))
            .withTitle(a($String("title")))
            .withPicture(a($URL()))
            .withType(oneOf(POIType.values()));
    }

    @NotNull
    public static NewsItemBuilder $NewsItem() {
        return new NewsItemBuilder()
            .withUuid(a($UUID()))
            .withContent(a($String("content")))
            .withCreated(a($Instant()));
    }
}
