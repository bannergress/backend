package com.bannergress.backend.restrictedarea;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.envers.Audited;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.MultiPolygon;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Represents a restricted area. */
@Entity
@Table(name = "restricted_area")
@Audited
public class RestrictedArea {
    /** Internal ID without further meaning. */
    @Id
    @Column(name = "uuid")
    @GeneratedValue
    private UUID uuid;

    /** Title. */
    @Column(name = "title")
    @NotEmpty
    private String title;

    /** Description. */
    @Column(name = "description")
    private String description;

    /** Area. */
    @Column(name = "area")
    private MultiPolygon area;

    /** Area relevant for Ingress. */
    @Column(name = "ingress_relevant_area")
    private MultiPolygon ingressRelevantArea;

    /** Restrictions. */
    @ElementCollection
    @CollectionTable(name = "restricted_area_restriction", joinColumns = @JoinColumn(name = "area"))
    @Column(name = "restriction")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Set<RestrictionType> restrictions = new HashSet<>();

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
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

    public MultiPolygon getIngressRelevantArea() {
        return ingressRelevantArea;
    }

    public void setIngressRelevantArea(MultiPolygon ingressRelevantArea) {
        this.ingressRelevantArea = ingressRelevantArea;
    }

    public Set<RestrictionType> getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(Set<RestrictionType> restrictions) {
        this.restrictions = restrictions;
    }
}
