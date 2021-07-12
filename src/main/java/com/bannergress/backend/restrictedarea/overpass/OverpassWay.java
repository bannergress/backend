package com.bannergress.backend.restrictedarea.overpass;

import java.util.List;

class OverpassWay extends OverpassGeometry {
    private List<OverpassCoordinate> geometry;

    public List<OverpassCoordinate> getGeometry() {
        return geometry;
    }

    public void setGeometry(List<OverpassCoordinate> geometry) {
        this.geometry = geometry;
    }
}
