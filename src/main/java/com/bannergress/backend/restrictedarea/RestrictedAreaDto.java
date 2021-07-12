package com.bannergress.backend.restrictedarea;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.locationtech.jts.geom.MultiPolygon;

import java.util.Set;
import java.util.UUID;

@JsonInclude(Include.NON_NULL)
public class RestrictedAreaDto {
    private UUID id;

    @NotEmpty
    private String title;

    private String description;

    @NotNull
    private MultiPolygon area;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private MultiPolygon ingressRelevantArea;

    @NotNull
    private Set<RestrictionType> restrictions;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MultiPolygon getArea() {
        return area;
    }

    public void setArea(MultiPolygon area) {
        this.area = area;
    }

    public Set<RestrictionType> getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(Set<RestrictionType> restrictions) {
        this.restrictions = restrictions;
    }

    public MultiPolygon getIngressRelevantArea() {
        return ingressRelevantArea;
    }

    public void setIngressRelevantArea(MultiPolygon ingressRelevantArea) {
        this.ingressRelevantArea = ingressRelevantArea;
    }
}
