package com.bannergress.backend.restrictedarea.overpass;

import java.util.List;

class OverpassRelation extends OverpassGeometry {
    private List<OverpassGeometry> members;

    public List<OverpassGeometry> getMembers() {
        return members;
    }

    public void setMembers(List<OverpassGeometry> members) {
        this.members = members;
    }
}
