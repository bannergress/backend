package com.bannergress.backend.user.keycloak;

import java.util.List;
import java.util.Map;

/** Partial keycloak user representation, as defined in https://www.keycloak.org/docs-api/latest/rest-api/index.html#UserRepresentation. */
public class KeycloakUserRepresentation {
    private Map<String, List<String>> attributes;

    public Map<String, List<String>> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, List<String>> attributes) {
        this.attributes = attributes;
    }
}
