package com.bannergress.backend.restrictedarea;

import java.util.Set;

public class RestrictedAreaSuggestion {
    private String id;

    private String title;

    private Set<RestrictionType> restrictions;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Set<RestrictionType> getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(Set<RestrictionType> restrictions) {
        this.restrictions = restrictions;
    }
}
