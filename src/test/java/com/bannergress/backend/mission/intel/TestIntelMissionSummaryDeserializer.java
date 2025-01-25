package com.bannergress.backend.mission.intel;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class TestIntelMissionSummaryDeserializer {
    @Autowired
    private JacksonTester<IntelMissionSummary> json;

    @Test
    public void testDeserialization() throws Exception {
        String data = "[\"901c3e75fc554d52ac04131c0b21fe2b.1c\",\"Target OAS\",\"https://lh4.ggpht.com/RHuFRkO9hyQJyYhESEWlfEydUgR7LFmEI8Vh8R1wNv0v5mIUfTBW1-o3E1N8Y8tTvuxU51RByRXaLPURwsq9\",\"777778\",\"3300830\"]";
        IntelMissionSummary summary = json.parseObject(data);
        assertThat(summary.averageDurationMilliseconds).isEqualTo(3300830);
        assertThat(summary.id).isEqualTo("901c3e75fc554d52ac04131c0b21fe2b.1c");
        assertThat(summary.picture).isEqualTo(new URL(
            "https://lh4.ggpht.com/RHuFRkO9hyQJyYhESEWlfEydUgR7LFmEI8Vh8R1wNv0v5mIUfTBW1-o3E1N8Y8tTvuxU51RByRXaLPURwsq9"));
        assertThat(summary.ratingE6).isEqualTo(777778);
        assertThat(summary.title).isEqualTo("Target OAS");
    }
}
