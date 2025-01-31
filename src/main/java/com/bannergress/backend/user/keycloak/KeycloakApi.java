package com.bannergress.backend.user.keycloak;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PutExchange;

public interface KeycloakApi {
    @GetExchange("/users/{userId}")
    KeycloakUserRepresentation getUser(@PathVariable String userId);

    @PutExchange("/users/{userId}")
    void updateUser(@PathVariable String userId, @RequestBody KeycloakUserRepresentation user);
}
