package com.bannergress.backend.utils;

import com.bannergress.backend.entities.Mission;
import com.bannergress.backend.entities.MissionStepBuilder;
import com.bannergress.backend.entities.POI;
import com.bannergress.backend.entities.POIBuilder;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestDistanceCalculation {
    @Test
    void test() {
        Mission mission1 = new Mission();
        POI frankfurt = new POIBuilder().withLatitude(50.110556).withLongitude(8.682222).build();
        POI bielefeld = new POIBuilder().build();
        mission1.getSteps().add(new MissionStepBuilder().withPoi(frankfurt).build());
        mission1.getSteps().add(new MissionStepBuilder().withPoi(bielefeld).build());
        assertThat(DistanceCalculation.calculateLengthMeters(List.of(mission1))).isEqualTo(0);

        Mission mission2 = new Mission();
        POI augsburg = new POIBuilder().withLatitude(48.371667).withLongitude(10.898333).build();
        POI nuremberg = new POIBuilder().withLatitude(49.455556).withLongitude(11.078611).build();
        POI hamburg = new POIBuilder().withLatitude(53.550556).withLongitude(9.993333).build();
        mission2.getSteps().add(new MissionStepBuilder().withPoi(augsburg).build());
        mission2.getSteps().add(new MissionStepBuilder().withPoi(nuremberg).build());
        mission2.getSteps().add(new MissionStepBuilder().withPoi(hamburg).build());
        assertThat(DistanceCalculation.calculateLengthMeters(List.of(mission2))).isCloseTo(582_720, Offset.offset(10));

        assertThat(DistanceCalculation.calculateLengthMeters(List.of(mission1, mission2))).isCloseTo(834_240,
            Offset.offset(10));
    }
}
