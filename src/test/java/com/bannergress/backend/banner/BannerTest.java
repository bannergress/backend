package com.bannergress.backend.banner;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BannerTest {
    @Test
    void getEfficiency() {
        // WHEN
        final Banner banner = new BannerBuilder().withNumberOfMissions(18).withLengthMeters(4_500).build();

        // VERIFY
        assertThat(banner.getEfficiency()).isEqualTo(4.0);
    }

    @Test
    void getEfficiency_unknownLength() {
        // WHEN
        final Banner banner = new BannerBuilder().withNumberOfMissions(18).withLengthMeters(null).build();

        // VERIFY
        assertThat(banner.getEfficiency()).isNull();
    }

    @Test
    void getEfficiency_zeroLength() {
        // WHEN
        final Banner banner = new BannerBuilder().withNumberOfMissions(18).withLengthMeters(0).build();

        // VERIFY
        assertThat(banner.getEfficiency()).isNull();
    }
}
