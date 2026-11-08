package com.bannergress.backend.comment;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class CommentSettingsTest {
    @Test
    void productionRejectsExemptionAndEnvironmentRealmMismatch() {
        assertThatThrownBy(() -> CommentIntegrationTest.settings("PROD", true, true, true, 3, 10))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CommentSettings(true, "TEST", true, false, 3, 10, 100,
            "bot", Set.of(123L), "https://login.bannergress.com/auth/realms/bannergress"))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CommentSettings(true, "PROD", false, true, 3, 10, 100,
            "bot", Set.of(123L), "https://login.bannergress.com/auth/realms/bannergress-test"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void defaultsDoNotBreakExistingTestDeploymentAndDoNotEnableComments() {
        try (var context = new AnnotationConfigApplicationContext()) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
                "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://login.bannergress.com/auth/realms/bannergress-test");
            context.register(CommentSettings.class);
            context.refresh();
            var settings = context.getBean(CommentSettings.class);
            assertThat(settings.enabled()).isFalse();
            assertThat(settings.allowUnlinkedTestUsers()).isFalse();
            assertThat(settings.verifiedAttributeTrusted()).isFalse();
            assertThat(settings.moderatorIds()).isEmpty();
        }
    }
}
