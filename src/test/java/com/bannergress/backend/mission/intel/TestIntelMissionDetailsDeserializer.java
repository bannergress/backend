package com.bannergress.backend.mission.intel;

import com.bannergress.backend.agent.Faction;
import com.bannergress.backend.mission.MissionType;
import com.bannergress.backend.mission.step.Objective;
import com.bannergress.backend.poi.POIType;
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

    @Test
    public void testDeserializationUnknownPlayer() throws Exception {
        String data = "[\"e60b228c4ceb4223bbbc7103fee42009.1c\",\"\\u4e09\\u8fc7\\u56fe\\u4e66\\u9986\\u4e0d\\u5165-18\",\"In recognition of the memory in Southeast University Nine Dragons' Lake Campus. Suggested transportation: bike. Mission starts from 18. Please do not do extra operations during the mission. Enjoy!\",\"unknown\",\"N\",866667,2315222,28,1,[[false,\"c1f56bf1d0af41969d1f35b8cdce9d6a.16\",\"\\u4e1c\\u5357\\u5927\\u5b66\\u4e5d\\u9f99\\u6e56\\u6821\\u533a\\u4e1c\\u95e8\",1,2,[\"p\",\"R\",31889332,118825042,1,70,1,\"http://lh3.googleusercontent.com/fVwRPA33Aw5vNbU1GU8IzZ90PNzdikmdebUvf_FLQUJfJiEP4Yp8d_FYlNZjGKZ7NvnyieDF97CmAK9QQKRANnjgqHY\",\"\\u4e1c\\u5357\\u5927\\u5b66\\u4e5d\\u9f99\\u6e56\\u6821\\u533a\\u4e1c\\u95e8\",[\"sc5_p\"],true,true,null,1619432947489]],[false,\"4b2e43708e604bca8faf4cf02743c136.16\",\"\\u7126\\u5ef7\\u6807\\u9986\",1,1,[\"p\",\"N\",31890354,118820929,1,0,0,\"http://lh3.googleusercontent.com/Rozbjr1GFsSmf_ZWALw8WUda9pK8u8EIsgdvJ-LP09Nq-j1ZILlrlNSyCPmW-V-t6CY349F_oRkORIHTxNSS0OS-woLO\",\"\\u7126\\u5ef7\\u6807\\u9986\",[\"sc5_p\"],true,false,null,1610115680148]],[false,\"3c60607c4ab3496b85f4ed943b2211b4.16\",\"\\u4e5d\\u9f99\\u6e56\\u94a2\\u94c1\\u83ca\\u82b1\",1,1,[\"p\",\"N\",31888992,118821008,1,0,0,\"http://lh3.googleusercontent.com/7W7vf4CBIVi4_9kp1ASzejMCIBPEiBiCtXYMR0rhNXgpuaS7wA8rPIamgzk25A7Ryzpy3dmF2h659qv2J1gNzTQrNqc6\",\"\\u4e5d\\u9f99\\u6e56\\u94a2\\u94c1\\u83ca\\u82b1\",[],true,false,null,1609661381444]],[false,\"998a1a68ac3c48ff8f34f231dae006aa.16\",\"\\u7b2c\\u4e00\\u6559\\u5b66\\u697c\",1,1,[\"p\",\"N\",31889166,118817204,1,0,0,\"http://lh3.googleusercontent.com/tAndN4JKEVRPCqdYbhKNwsHC27kWi2mn02yKZg781zcfWHT-vXEzZjqxpZXz399p215knTvb_K9ylHd7AEUC_FYDF8U\",\"\\u7b2c\\u4e00\\u6559\\u5b66\\u697c\",[],false,false,null,1609423430196]],[false,\"e29bb61834d14dd186fe2828c8f6e133.16\",\"\\u5b54\\u5b50\\u5851\\u50cf\",1,8,[\"p\",\"N\",31889015,118815326,1,0,0,\"http://lh3.googleusercontent.com/vP8BcBbCvXZcGzLrmC5MX5M8VIEa56ASU1yj0zPIThQ_M3Oe_5WMmUhF0lOXD7iWl7oJefjIfCjGYIp52qk14IApyg6i\",\"\\u5b54\\u5b50\\u5851\\u50cf\",[\"sc5_p\"],true,true,null,1609411348936]],[false,\"df5d604c192847e7bddc0a91813770db.16\",\"\\u674e\\u6587\\u6b63\\u56fe\\u4e66\\u9986\",1,1,[\"p\",\"N\",31889162,118814078,1,0,0,\"http://lh3.googleusercontent.com/PUMlrVUotsjdbebHP7l7go9aB4sHfYDVwyeKdBN6pKSyiXA1ic5QO3BG3TRVlFSj_g6imknqHIrXYFqux6S89OR3l7CY\",\"\\u674e\\u6587\\u6b63\\u56fe\\u4e66\\u9986\",[],true,true,null,1609411219953]]],\"https://lh3.googleusercontent.com/W727-3LqgMNyB4LD14Q8JuV66u_6lfQaL7JKCjfNxI49FYZ8KBFJcWDHIoS_C_S7cjZ-_FCw-4SCuPMxIKpA\"]";
        ObjectMapper mapper = new ObjectMapper();
        IntelMissionDetails details = mapper.readValue(data, IntelMissionDetails.class);
        assertThat(details.authorFaction).isNull();
        assertThat(details.authorName).isEqualTo("unknown");
    }

    @Test
    public void testDeserializationHidden() throws Exception {
        String data = "[\"c0daa664c12e4507a3549d535351e777.1c\",\"\\u5357\\u84dd\\u4eba\\u7269\\u5fd7\\u4e4b\\u2014\\u2014\\u7f9e\\u7f9e\\u5929\\u7bc7\",\"\\u7f9e\\u7f9e\\u5929\\uff0c\\u5357\\u84dd\\u8457\\u540d\\u63a2\\u5458\\uff0c\\u6211\\u4eec\\u6765\\u770b\\u770b\\u4ed6\\u5728\\u4e1c\\u5357\\u5927\\u5b66\\u4e5d\\u9f99\\u6e56\\u6821\\u533a\\u7559\\u4e0b\\u4e86\\u600e\\u6837\\u7684\\u4f20\\u5947\\u6545\\u4e8b\\u2026\\u2026\",\"CaptainArt\",\"R\",1000000,1634202,3,3,[[false,\"073ba19437ac4fefa61dfe8266f64edd.16\",\"\\u4e1c\\u5357\\u5927\\u5b66\\u5357\\u5927\\u95e8\",1,1,[\"p\",\"N\",31883130,118814760,1,0,0,\"http://lh3.googleusercontent.com/CoUP13qHlHZfgImCjKAoRGY1KtqgZz1tYWXgOvZV117l3_l7cyf8FlM14HnN0z8QCYxUaBteW84dotBXmLxyZG5lPXA\",\"\\u4e1c\\u5357\\u5927\\u5b66\\u5357\\u5927\\u95e8\",[],true,true,null,1609497344766]],[true,\"\",\"\",0,0,null],[true,\"\",\"\",0,0,null],[true,\"\",\"\",0,0,null],[true,\"\",\"\",0,0,null],[true,\"\",\"\",0,0,null]],\"https://lh3.googleusercontent.com/oYDl-eKGbMzjwKQuvjTicLbe5Gix5uA9EwZIVKW4Wbu6VQez8R9pE45lvjNdGvrI8q0eiaPeNWDt3uYA4Jw\"]";
        ObjectMapper mapper = new ObjectMapper();
        IntelMissionDetails details = mapper.readValue(data, IntelMissionDetails.class);
        assertThat(details.steps.get(0).hidden).isFalse();
        assertThat(details.steps.get(0).objective).isNotNull();
        assertThat(details.steps.get(1).hidden).isTrue();
        assertThat(details.steps.get(1).objective).isNull();
    }

    @Test
    public void testDeserializationWithNullPortalPicture() throws Exception {
        String data = "[\"3b22b6ec2b9445a4a49f48437c47077b.1c\",\"Донецк\",\"9из 46. Герб Украины. Название следующей вы узнаете  на последнем портале.\",\"Doomka\",\"E\",0,454036,1,1,[[false,\"05b240ed6f3f48debf9f1590a09d3585.12\",\"М. Gorky\",1,1,[\"p\",\"N\",46489951,30739562,1,0,0,null,\"М. Gorky\",[],false,false,null,1687074629721]],[false,\"533c84ca150345e0b0b942a77a5be3ab.11\",\"Воронцовский Дворец\",1,1,[\"p\",\"N\",46489949,30738770,1,0,0,null,\"Воронцовский Дворец\",[],false,false,null,1687074629721]],[false,\"8e7c7fbc7aa8455f89cf6e8673254e69.12\",\"Lion near the Vorontsov Palace\",1,1,[\"p\",\"N\",46490260,30738472,1,0,0,null,\"Lion near the Vorontsov Palace\",[],false,false,null,1687074629721]],[false,\"024fd530ba1f45f7985e1cf58f127355.11\",\"Colonnade\",1,1,[\"p\",\"N\",46490717,30738470,1,0,0,null,\"Colonnade\",[],false,false,null,1687074629721]],[false,\"03b39cf063be4a9683df47d191b8a933.12\",\"Тёщин Мост\",1,1,[\"p\",\"N\",46490481,30737960,1,0,0,null,\"Тёщин Мост\",[],false,false,null,1687074629721]],[false,\"d890bc1a2c8d40739500a4bdd9f06758.16\",\"Ретро Авто\",1,1,[\"p\",\"N\",46490258,30737203,1,0,0,null,\"Ретро Авто\",[],false,false,null,1687074629721]],[false,\"5443962f4895441aaac3031eec0450c3.16\",\"Фонтан, старая Одесса\",1,1,[\"p\",\"N\",46490637,30737050,1,0,0,null,\"Фонтан, старая Одесса\",[],false,false,null,1687074629721]],[false,\"925c5630a9ad41bba61965edebfa9386.12\",\"Griffon, Old Odessa\",1,1,[\"p\",\"N\",46490231,30736709,1,0,0,null,\"Griffon, Old Odessa\",[],false,false,null,1687074629721]],[false,\"50f206c6ad944c32b5068d34c59051b9.16\",\"Graffiti on Prymors'ka Street\",1,8,[\"p\",\"N\",46490711,30736506,1,0,0,null,\"Graffiti on Prymors'ka Street\",[],false,false,null,1687074629721]],[false,\"68a9074307b84fcea263796e5b5b6d2f.12\",\"Чугунный Мостик\",1,8,[\"p\",\"N\",46490428,30736234,1,0,0,null,\"Чугунный Мостик\",[],false,false,null,1687074629721]]],\"https://lh6.ggpht.com/hRn-6HmXcAKjvcYGL-0URyvyqOJ-MmUvrUzlVf1kQTZms_tBbBecBFO5gPIc_nUNxQySIj327OqrWg2X3OQB\"]";
        ObjectMapper mapper = new ObjectMapper();
        IntelMissionDetails details = mapper.readValue(data, IntelMissionDetails.class);
        assertThat(details.steps.get(0).picture).isNull();
    }
}
