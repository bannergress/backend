package com.bannergress.backend.user.keycloak;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class KeycloakUserMappingServiceImplTest {
    @Test
    void missingEmptyBlankAndAmbiguousMappingsAreNotVerified() {
        var api = mock(KeycloakApi.class);
        var service = new KeycloakUserMappingServiceImpl();
        ReflectionTestUtils.setField(service, "keycloakApi", api);
        var user = new KeycloakUserRepresentation();
        when(api.getUser("user")).thenReturn(user);
        assertThat(service.getAgentName("user")).isEmpty();
        var attributes = new HashMap<String, List<String>>();
        user.setAttributes(attributes);
        for (var values : List.of(List.<String>of(), List.of(" "), List.of("one", "two"))) {
            attributes.put("agent", values);
            assertThat(service.getAgentName("user")).isEmpty();
        }
        attributes.put("agent", null);
        assertThat(service.getAgentName("user")).isEmpty();
        attributes.put("agent", List.of("VerifiedAgent"));
        assertThat(service.getAgentName("user")).contains("VerifiedAgent");
    }
}
