package com.bannergress.backend.dto.serialization;

import com.bannergress.backend.dto.IntelMissionDetails;
import com.bannergress.backend.enums.Faction;
import com.bannergress.backend.enums.MissionType;
import com.bannergress.backend.enums.Objective;
import com.bannergress.backend.enums.POIType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

public class TestIntelMissionDetailsDeserializer {
    @Test
    public void testDeserialization() throws Exception {
        String data = "[\"fa091a67821d4b0391de326adb4dae52.1c\",\"Barcode Banner #3\",\"The Barcode is a section of the Bj\\u00f8rvika portion of the Fjord City redevelopment on former dock and industrial land in central Oslo. It consists of a row of new multi-purpose high-rise buildings.\",\"Sechesan\",\"R\",877729,708916,232,2,[[false,\"bb3093528dac4079b92d203792e880f6.16\",\"Pyramidene\",1,1,[\"p\",\"R\",59907563,10760350,6,100,8,\"http://lh3.googleusercontent.com/zFgxCki5cjukHYiT9y95hLcL4ehNIKLgRV1kGn0jk-KF-gHz1hSncNnV5NlTQnwYhBZt81iu1eaKNgyaK-oQ-S5OnQ\",\"Pyramidene\",[],true,true,null,1618848907202]],[false,\"397fd7a003544854d9fe0d4e61cabb77.1d\",\"A-Lab adds pixelated tower with a hollow centre  to Oslo's new waterfront development\",2,7,[\"f\",59907859,10760899]],[false,\"731bc55f5931f67dd5f9651a9e1aa37b.1d\",\"DnB NOR Bygg B\",2,7,[\"f\",59907649,10761190]],[false,\"7317828ab21d4d8d84ada97fad2c9933.16\",\"Ladegarden fontene\",1,1,[\"p\",\"R\",59906148,10766717,2,100,8,\"http://lh3.googleusercontent.com/cjUsEaURZNSTialJ1gCX9VfbLdJVdiD-EGyJciIlECzQwzJrWMwlc4h0LLB7OPZwVYqyMIJ3toGyCNhxA8w_QQktpDg\",\"Ladegarden fontene\",[],true,true,null,1618859072574]],[false,\"34bc430b61994cac9e25f175381d9057.16\",\"Ladegaard\",1,1,[\"p\",\"R\",59906096,10767407,6,100,8,\"http://lh3.googleusercontent.com/m5dTu7FVaTQE69rrYY_G-Kvgte07I7amRkYR2-aos30cfRfBNuyp3uamlIsexLsV_QKOXQ_WCXwltBD5bRoRzSmyQzU\",\"Ladegaard\",[],true,true,null,1618849007445]],[false,\"ca03894732de4e89ab1840cd86a3878e.16\",\"St. Hallvardskatedralen\",1,1,[\"p\",\"R\",59906057,10768108,1,100,1,\"http://lh3.googleusercontent.com/5zls9EOzCCbR4Qan9P8bWTCNIweVLbQ3rRUaUygaDBJeH8GnjRhBrLs8FuoZnesepl7yvGfpwacQM6V9sNszPj7heMg\",\"St. Hallvardskatedralen\",[],true,true,null,1618839952281]]],\"https://lh3.googleusercontent.com/vnWHzlzQUVBSyXeEkw8Wsa3N-qKKL4ZI6TcgSkSMLyqcmLFgLatFONo1tiNHEUeZCCYN3veQqIQHeO_x-4o\"]";
        ObjectMapper mapper = new ObjectMapper();
        IntelMissionDetails details = mapper.readValue(data, IntelMissionDetails.class);
        assertThat(details.authorFaction).isEqualTo(Faction.resistance);
        assertThat(details.authorName).isEqualTo("Sechesan");
        assertThat(details.averageDurationMilliseconds).isEqualTo(708916);
        assertThat(details.description).isEqualTo("The Barcode is a section of the Bj\u00f8rvika portion of the Fjord City redevelopment on former dock and industrial land in central Oslo. It consists of a row of new multi-purpose high-rise buildings.");
        assertThat(details.id).isEqualTo("fa091a67821d4b0391de326adb4dae52.1c");
        assertThat(details.numberCompleted).isEqualTo(232);
        assertThat(details.picture).isEqualTo(new URL("https://lh3.googleusercontent.com/vnWHzlzQUVBSyXeEkw8Wsa3N-qKKL4ZI6TcgSkSMLyqcmLFgLatFONo1tiNHEUeZCCYN3veQqIQHeO_x-4o"));
        assertThat(details.ratingE6).isEqualTo(877729);
        assertThat(details.steps.size()).isEqualTo(6);
        assertThat(details.steps.get(0).hidden).isEqualTo(false);
        assertThat(details.steps.get(0).id).isEqualTo("bb3093528dac4079b92d203792e880f6.16");
        assertThat(details.steps.get(0).latitudeE6).isEqualTo(59907563);
        assertThat(details.steps.get(0).longitudeE6).isEqualTo(10760350);
        assertThat(details.steps.get(0).objective).isEqualTo(Objective.hack);
        assertThat(details.steps.get(0).picture).isEqualTo(new URL("http://lh3.googleusercontent.com/zFgxCki5cjukHYiT9y95hLcL4ehNIKLgRV1kGn0jk-KF-gHz1hSncNnV5NlTQnwYhBZt81iu1eaKNgyaK-oQ-S5OnQ"));
        assertThat(details.steps.get(0).title).isEqualTo("Pyramidene");
        assertThat(details.steps.get(0).type).isEqualTo(POIType.portal);
        assertThat(details.steps.get(1).hidden).isEqualTo(false);
        assertThat(details.steps.get(1).id).isEqualTo("397fd7a003544854d9fe0d4e61cabb77.1d");
        assertThat(details.steps.get(1).latitudeE6).isEqualTo(59907859);
        assertThat(details.steps.get(1).longitudeE6).isEqualTo(10760899);
        assertThat(details.steps.get(1).objective).isEqualTo(Objective.viewWaypoint);
        assertThat(details.steps.get(1).picture).isEqualTo(null);
        assertThat(details.steps.get(1).title).isEqualTo("A-Lab adds pixelated tower with a hollow centre  to Oslo's new waterfront development");
        assertThat(details.steps.get(1).type).isEqualTo(POIType.fieldTripWaypoint);
        assertThat(details.title).isEqualTo("Barcode Banner #3");
        assertThat(details.type).isEqualTo(MissionType.anyOrder);
    }
}
