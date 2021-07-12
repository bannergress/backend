package com.bannergress.backend.restrictedarea.overpass;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import java.util.HashMap;
import java.util.Map;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@Type(value = OverpassArea.class, name = "area"), @Type(value = OverpassWay.class, name = "way"),
    @Type(value = OverpassRelation.class, name = "relation"), @Type(value = OverpassNode.class, name = "node")})
abstract class OverpassGeometry {
    private String id;

    private String role;

    private Map<String, String> tags = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }
}
