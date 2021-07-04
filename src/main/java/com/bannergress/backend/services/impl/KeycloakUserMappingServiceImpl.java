package com.bannergress.backend.services.impl;

import com.bannergress.backend.services.UserMappingService;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * User mapping service that uses Keycloak.
 */
@Service
@Profile("!dev")
public class KeycloakUserMappingServiceImpl implements UserMappingService {
    private static final String AGENT_ATTRIBUTE = "agent";

    private final UsersResource usersResource;

    public KeycloakUserMappingServiceImpl(@Value(value = "${keycloak.auth-server-url}") String serverUrl,
        @Value(value = "${keycloak.realm}") String realm, @Value(value = "${keycloak.resource}") String clientId,
        @Value(value = "${keycloak.credentials.secret}") String clientSecret) {
        Keycloak keycloak = KeycloakBuilder.builder() //
            .serverUrl(serverUrl) //
            .realm(realm) //
            .grantType(OAuth2Constants.CLIENT_CREDENTIALS) //
            .clientId(clientId) //
            .clientSecret(clientSecret) //
            .build();
        usersResource = keycloak.realm(realm).users();
    }

    @Override
    public void setAgentName(String user, String agent) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.singleAttribute(AGENT_ATTRIBUTE, agent);
        usersResource.get(user).update(userRepresentation);
    }

    @Override
    public Optional<String> getAgentName(String user) {
        UserRepresentation userRepresentation = usersResource.get(user).toRepresentation();
        return Optional.ofNullable(userRepresentation.firstAttribute(AGENT_ATTRIBUTE));
    }
}
