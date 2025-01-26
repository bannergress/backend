package com.bannergress.backend.mission.creator;

import com.bannergress.backend.mission.MissionType;
import com.bannergress.backend.mission.step.Objective;
import com.bannergress.backend.poi.POIType;
import com.fasterxml.jackson.core.JacksonException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JsonTest
public class TestCreatorGetMissionForProfileDeserializer {
    @Autowired
    private JacksonTester<CreatorGetMissionForProfile> json;

    @Test
    public void testDeserialization() throws Exception {
        String data = "{\"request\":{\"mission_guid\":\"e81b46ebb9e44e60bc1fcf44d77edc31.1c\"},\"response\":{\"mission\":{\"definition\":{\"author_nickname\":\"westfellow\",\"badge_url\":\"https://lh3.googleusercontent.com/GDR5NzFoS5gyOkHogp15z2QYZEEWwq0fuYp9OA3SfLEFc-AKf9IAGijmvrhyh9-2a03FjTMXDRinlLWc3bNa40YVszuGujeTPw\",\"description\":\"Take a walk through Fort Greene Brooklyn and discover the inner workings of toast. Walk past the Lafayette Avenue C train station, formerly the IND Fulton Street Line .\",\"guid\":\"e81b46ebb9e44e60bc1fcf44d77edc31.1c\",\"logo_url\":\"https://lh3.googleusercontent.com/clLYd_RCBndbs5v6msYvprIw2giT62A-H-FaJYylbPFzZnxg8m-dYzQK5csTxjV0Hrro5RLIaINn6An5QpHle99Nkcwb7wZV\",\"mission_type\":\"NON_SEQUENTIAL\",\"name\":\"Toast Anatomy 5 of 6\",\"waypoints\":[{\"custom_description\":\"\",\"hidden\":false,\"hidden_location_clue\":null,\"objective\":{\"passphrase_params\":null,\"type\":\"HACK_PORTAL\"},\"poi_guid\":\"04dab0f5648f4ccfb986960fc47d1f02.16\",\"poi_type\":null},{\"custom_description\":\"\",\"hidden_location_clue\":null,\"objective\":{\"passphrase_params\":null,\"type\":\"HACK_PORTAL\"},\"poi_guid\":\"5d26d247d0934e6fab5b852b071c207c.16\",\"poi_type\":null},{\"custom_description\":\"\",\"hidden_location_clue\":null,\"objective\":{\"passphrase_params\":null,\"type\":\"HACK_PORTAL\"},\"poi_guid\":\"22f1ec69d7524c3e8a50bda87e32acc9.16\",\"poi_type\":null},{\"custom_description\":\"\",\"hidden_location_clue\":null,\"objective\":{\"passphrase_params\":null,\"type\":\"HACK_PORTAL\"},\"poi_guid\":\"8538b548808b428890a41f6d2b7fafdc.16\",\"poi_type\":null},{\"custom_description\":\"\",\"hidden_location_clue\":null,\"objective\":{\"passphrase_params\":null,\"type\":\"VIEW_FIELD_TRIP_CARD\"},\"poi_guid\":\"8fa8646556490aa1c4b25b7732688a96.1d\",\"poi_type\":null},{\"custom_description\":\"\",\"hidden_location_clue\":null,\"objective\":{\"passphrase_params\":null,\"type\":\"VIEW_FIELD_TRIP_CARD\"},\"poi_guid\":\"2494e8a2729f718e2903db6576d898da.1d\",\"poi_type\":null}]},\"stats\":{\"median_completion_time\":466283,\"num_completed\":3,\"rating\":100}},\"pois\":[{\"description\":\"\",\"guid\":\"04dab0f5648f4ccfb986960fc47d1f02.16\",\"imageUrl\":\"http://lh3.googleusercontent.com/eMfdwD0GW8HZ7W22R0q0UVvUzAm1lh5z8bPijupXDSnPokKVxXRt6OrP4FymvWKZfwkBbDtExjhlMXkawWnbl7YjNq3_\",\"isOrnamented\":true,\"isStartPoint\":true,\"location\":{\"latitude\":40.686511,\"longitude\":-73.97537},\"title\":\"Trap Art\",\"type\":\"PORTAL\"},{\"description\":\"\",\"guid\":\"5d26d247d0934e6fab5b852b071c207c.16\",\"imageUrl\":\"http://lh3.googleusercontent.com/FSOIF3YOPgNugjBQ8DQggWSXypkZ4u35xwKEO6Rpl76b2zVin2BQ7N_U9XuSYEsbxrEaB7XG49PP4I9WgOollMdcyNg\",\"isOrnamented\":false,\"isStartPoint\":false,\"location\":{\"latitude\":40.686704,\"longitude\":-73.975139},\"title\":\"Black Forest Brooklyn\",\"type\":\"PORTAL\"},{\"description\":\"Local independent bookstore, home to many literary events.\",\"guid\":\"22f1ec69d7524c3e8a50bda87e32acc9.16\",\"imageUrl\":\"http://lh3.googleusercontent.com/Wlh_JDxFXdpN9RkinH7H-HJo754Oqo2hc5UXh0i-33yuhi6xrf4lYVcYeHO_hYfOdjEiwwBT5uPBEsy71jx3XQlyElbv\",\"isOrnamented\":false,\"isStartPoint\":false,\"location\":{\"latitude\":40.686256,\"longitude\":-73.974566},\"title\":\"Greenlight Bookstore\",\"type\":\"PORTAL\"},{\"description\":\"This realistic trompe-l'œil enamelware was installed by the Estonian conceptual artist Toomas Hyuuz in 1999.\",\"guid\":\"8538b548808b428890a41f6d2b7fafdc.16\",\"imageUrl\":\"http://lh3.googleusercontent.com/kE9cqmYzuZxhH3zm9MVgfI8DleLKzoZd39SDxjQ5nNh-w1tS1217RUEP79LEtaBSQ46faRIJcfOjGBf75pzjOmgvIWnL\",\"isOrnamented\":false,\"isStartPoint\":false,\"location\":{\"latitude\":40.686524,\"longitude\":-73.974577},\"title\":\"Lafayette Avenue Station\",\"type\":\"PORTAL\"},{\"description\":\"<div><table border=\\\"0\\\" cellpadding=\\\"2\\\" cellspacing=\\\"2\\\" style=\\\"vertical-align:top;\\\"> <tr> <td align=\\\"left\\\" valign=\\\"top\\\"> <a class=\\\"p\\\" target=\\\"_blank\\\" href=\\\"http://www.tastingtable.com/entry_detail/nyc/8887/Double-Decker_Pastrami.htm?referrer=rss_fieldtrip&location=24375\\\"></a> </td> </tr> <tr> <td valign=\\\"top\\\" class=\\\"j\\\"> <div class=\\\"lh\\\"> <b><font color=\\\"#6f6f6f\\\">Two new pastramis, making lunch better one slice at a time</font></b> <br /> <p>We are a city united by our love for pastrami.</p><p>You can haggle over the classic versions available at the <a title=\\\"Tasting Table: 2nd Ave Deli\\\" href=\\\"http://www.tastingtable.com/entry_detail/nyc/5268/The_Second_2nd_Avenue_Deli.htm\\\">2nd Ave Deli</a> and Katz’s, or even the newfangled take at <a title=\\\"Tasting Table: Mile End\\\"></a><a class=\\\"p\\\" target=\\\"_blank\\\" href=\\\"http://www.tastingtable.com/entry_detail/nyc/8887/Double-Decker_Pastrami.htm?referrer=rss_fieldtrip&location=24375\\\"><nobr><b>Read More »</b></nobr></a> </p></div> </td> </tr> <tr><td> </td></tr></table></div>\",\"guid\":\"8fa8646556490aa1c4b25b7732688a96.1d\",\"imageUrl\":\"{\\\"photoUrl\\\":\\\"http://lh6.ggpht.com/k4KqyAU3zW6LsOC7wWKnN7NzofQsbkN-N1dG8QK6jQNiJsQDv4W0o3GwY6RvjixMIfdsm96GkhG6r7lC_QKQ_Q\\\"}\",\"isOrnamented\":false,\"isStartPoint\":false,\"location\":{\"latitude\":40.68658,\"longitude\":-73.974609},\"publisherInfo\":{\"blurb\":\"<p><a href=\\\"http://www.tastingtable.com/\\\">Tasting Table</a> - Tasting Table - The best of food and drink culture daily.\\r\\n</p>\",\"homepageUrl\":\"http://www.tastingtable.com/\",\"logoUrl\":\"http://lh4.ggpht.com/kgGoeqK5JpsacmMGIkLAZ9c8o-Uv86rPWerz3cAzzDsuxi3i94d9sgnivmoPD8lxfZflf4-TnnSPQWRSC6hYHw=s80\",\"name\":\"Tasting Table\"},\"title\":\"Greene Grape Provisions: Double-Decker Pastrami\",\"type\":\"FIELD_TRIP_CARD\"},{\"description\":\"<p><span class=\\\"credit\\\">Photos: WWD</span></p>\\n<p>Cobble Hill standby <strong>Shen Beauty</strong> has finally opened its <a href=\\\"http://ny.racked.com/archives/2014/08/28/shen_beauty_salon.php\\\">long-awaited</a> second shop at <b>88 South Portland Avenue</b>—and what's slated to be a <b>pop-up shop</b> in <b>Fort Greene</b> might not be so short-lived. <em>WWD</em> <a href=\\\"http://www.wwd.com/beauty-industry-news/retailing/shen-beauty-expands-its-brooklyn-footprint-8055690\\\">reports </a> that the store has just signed a <strong>six-month lease</strong>, and owner <b>Jessica Richards</b> hopes to stay for even longer.</p>\\n<p>\\\"The location is ideal due to the fact that there is <strong>no beauty in Fort Greene</strong> and there's such a diverse clientele,\\\" Richards told the paper. </p>\\n<p>While teensy at only <b>350 square feet</b>, the store carries all of Shen's cultish brands, including RMS Beauty, <strong>Kjaer Weis</strong>, <strong>Pai</strong>, By Terry, <strong>Amanda Lacey</strong> and de Mamiel (some of which were used to create this signature Cobble Hill <a href=\\\"http://ny.racked.com/archives/2014/11/24/nyc_neighborhood_beauty.php\\\">beauty look</a>).</p>\\n<p>And the Fort Greene store may also only be a taste of what's to come for the rest of Brooklyn. Back in August, Richards <a href=\\\"http://ny.racked.com/archives/2014/08/28/shen_beauty_salon.php\\\">hinted</a> that she's looking for a <strong>Williamsburg outpost</strong> as well. \\\"I would like to be the next <strong>Space NK</strong>,\\\" she said at the time.</p>\\n<p>· <a href=\\\"http://www.wwd.com/beauty-industry-news/retailing/shen-beauty-expands-its-brooklyn-footprint-8055690\\\">Shen Beauty Expands its Brooklyn Footprint</a> [WWD]<br />\\n· <a href=\\\"http://ny.racked.com/archives/2014/08/28/shen_beauty_salon.php\\\">Shen Beauty Plots Expansion as Co-Founder Leaves</a> [Racked NY]<br />\\n· <a href=\\\"http://ny.racked.com/archives/2014/11/24/nyc_neighborhood_beauty.php\\\">Can You Match the Beauty Look to Its NYC Neighborhood?</a> [Racked NY]</p>\",\"guid\":\"2494e8a2729f718e2903db6576d898da.1d\",\"imageUrl\":\"http://lh4.ggpht.com/hA98xnUxjU_BQTlzsGiJ6F2GjKL_922YhCyd1UgBK9Kot-o7lORH0fKm6UmYH-RgHrTnDYN9Kb499giCsXLeBdk6oBTpV6LbcA\",\"isOrnamented\":false,\"isStartPoint\":false,\"location\":{\"latitude\":40.686798,\"longitude\":-73.974601},\"publisherInfo\":{\"blurb\":\"<p><a href=\\\"http://racked.com/\\\">Racked</a> - Racked is all about retail and shopping—from sidewalks to catwalks.</p>\",\"homepageUrl\":\"http://racked.com/\",\"logoUrl\":\"http://lh3.ggpht.com/zNmRsmmZbSIRqiXVMeLWnk5mQZ0Os0w_ZzL-5ka14rlEkGqvV7VF8Q-jJ-MAs0UngU1RmwSKv-NWHp8mVQgXbQ=s80\",\"name\":\"Racked\"},\"title\":\"Open For Now: Shen Beauty's Fort Greene Store Is Open, Maybe Permanently\",\"type\":\"FIELD_TRIP_CARD\"}]}}";
        CreatorGetMissionForProfile parsed = json.parseObject(data);
        assertThat(parsed.request.mission_guid).isEqualTo("e81b46ebb9e44e60bc1fcf44d77edc31.1c");
        assertThat(parsed.response.mission.definition.author_nickname).isEqualTo("westfellow");
        assertThat(parsed.response.mission.definition.description).isEqualTo(
            "Take a walk through Fort Greene Brooklyn and discover the inner workings of toast. Walk past the Lafayette Avenue C train station, formerly the IND Fulton Street Line .");
        assertThat(parsed.response.mission.definition.guid).isEqualTo("e81b46ebb9e44e60bc1fcf44d77edc31.1c");
        assertThat(parsed.response.mission.definition.logo_url).isEqualTo(new URL(
            "https://lh3.googleusercontent.com/clLYd_RCBndbs5v6msYvprIw2giT62A-H-FaJYylbPFzZnxg8m-dYzQK5csTxjV0Hrro5RLIaINn6An5QpHle99Nkcwb7wZV"));
        assertThat(parsed.response.mission.definition.mission_type).isEqualTo(MissionType.anyOrder);
        assertThat(parsed.response.mission.definition.name).isEqualTo("Toast Anatomy 5 of 6");
        assertThat(parsed.response.mission.definition.waypoints.size()).isEqualTo(6);
        assertThat(parsed.response.mission.definition.waypoints.get(0).hidden).isFalse();
        assertThat(parsed.response.mission.definition.waypoints.get(0).objective.type).isEqualTo(Objective.hack);
        assertThat(parsed.response.mission.definition.waypoints.get(0).poi_guid)
            .isEqualTo("04dab0f5648f4ccfb986960fc47d1f02.16");
        assertThat(parsed.response.mission.definition.waypoints.get(4).objective.type)
            .isEqualTo(Objective.viewWaypoint);
        assertThat(parsed.response.pois.size()).isEqualTo(6);
        assertThat(parsed.response.pois.get(0).guid).isEqualTo("04dab0f5648f4ccfb986960fc47d1f02.16");
        assertThat(parsed.response.pois.get(0).imageUrl).isEqualTo(new URL(
            "http://lh3.googleusercontent.com/eMfdwD0GW8HZ7W22R0q0UVvUzAm1lh5z8bPijupXDSnPokKVxXRt6OrP4FymvWKZfwkBbDtExjhlMXkawWnbl7YjNq3_"));
        assertThat(parsed.response.pois.get(0).location.latitude).isEqualTo(40.686511);
        assertThat(parsed.response.pois.get(0).location.longitude).isEqualTo(-73.97537);
        assertThat(parsed.response.pois.get(0).title).isEqualTo("Trap Art");
        assertThat(parsed.response.pois.get(0).type).isEqualTo(POIType.portal);
        assertThat(parsed.response.pois.get(4).imageUrl).isEqualTo(new URL(
            "http://lh6.ggpht.com/k4KqyAU3zW6LsOC7wWKnN7NzofQsbkN-N1dG8QK6jQNiJsQDv4W0o3GwY6RvjixMIfdsm96GkhG6r7lC_QKQ_Q"));
        assertThat(parsed.response.pois.get(4).type).isEqualTo(POIType.fieldTripWaypoint);
    }

    @Test
    public void testDeserializationMissionNotFound() throws Exception {
        String data = "{\"request\":{\"mission_guid\":\"eec45a5fab474d62989a888f79069362.1c\"},\"response\":{\"mat_error\":{\"description\":\"guid: eec45a5fab474d62989a888f79069362.1c\",\"title\":\"Mission Not Found\"}}}";
        CreatorGetMissionForProfile parsed = json.parseObject(data);
        assertThat(parsed.request.mission_guid).isEqualTo("eec45a5fab474d62989a888f79069362.1c");
        assertThat(parsed.response.mat_error.title).isEqualTo(CreatorGetMissionForProfile.ErrorTitle.missionNotFound);
    }

    @Test
    public void testDeserializationUnauthorized() throws Exception {
        String data = "{\"request\":{\"mission_guid\":\"eec45a5fab474d62989a888f79069362.1c\"},\"response\":{\"mat_error\":{\"description\":\"Unable to verify authenticity. Please try again.\",\"title\":\"Authentication Error\"}}}";
        assertThatThrownBy(() -> json.parseObject(data)).isInstanceOf(JacksonException.class);
    }
}
