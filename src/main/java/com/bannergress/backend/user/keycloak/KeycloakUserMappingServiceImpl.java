package com.bannergress.backend.user.keycloak;

import com.bannergress.backend.user.UserMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.service.registry.ImportHttpServices;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * User mapping service that uses Keycloak.
 */
@Service
@ImportHttpServices(group = "keycloak", types = KeycloakApi.class)
class KeycloakUserMappingServiceImpl implements UserMappingService {
    private static final String AGENT_ATTRIBUTE = "agent";

    @Autowired
    private KeycloakApi keycloakApi;

    @Override
    public void setAgentName(String user, String agent) {
        KeycloakUserRepresentation userRepresentation = keycloakApi.getUser(user);
        userRepresentation.getAttributes().put(AGENT_ATTRIBUTE, Optional.ofNullable(agent).stream().toList());
        keycloakApi.updateUser(user, userRepresentation);
    }

    @Override
    public Optional<String> getAgentName(String user) {
        KeycloakUserRepresentation userRepresentation = keycloakApi.getUser(user);
        Map<String, List<String>> attributes = userRepresentation.getAttributes();
        if (attributes == null || !attributes.containsKey(AGENT_ATTRIBUTE)) {
            return Optional.empty();
        } else {
            List<String> agents = attributes.get(AGENT_ATTRIBUTE);
            if (agents == null || agents.size() != 1) {
                return Optional.empty();
            }
            return Optional.ofNullable(agents.getFirst()).filter(agent -> !agent.isBlank());
        }
    }
}
