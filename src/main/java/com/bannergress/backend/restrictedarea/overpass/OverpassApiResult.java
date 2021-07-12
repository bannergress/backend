package com.bannergress.backend.restrictedarea.overpass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
class OverpassApiResult {
    private List<OverpassGeometry> elements;

    public List<OverpassGeometry> getElements() {
        return elements;
    }

    public void setElements(List<OverpassGeometry> elements) {
        this.elements = elements;
    }
}
