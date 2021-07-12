package com.bannergress.backend.restrictedarea;

import com.bannergress.backend.entities.POI;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
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
    @Column(name = "uuid", columnDefinition = "uuid")
    @GeneratedValue
    private UUID uuid;

    /** Title. */
    @Column(name = "title", nullable = false)
    private String title;

    /** Description. */
    @Column(name = "description", nullable = false)
    private String description;

    /** Area. */
    @Column(name = "area", nullable = false)
    private MultiPolygon area;

    /** Area relevant for Ingress. */
    @Column(name = "ingress_relevant_area", nullable = false)
    private MultiPolygon ingressRelevantArea;

    /** Restrictions. */
    @ElementCollection
    @CollectionTable(name = "restricted_area_restriction", joinColumns = @JoinColumn(name = "area"))
    @Column(name = "restriction", nullable = false)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Set<RestrictionType> restrictions = new HashSet<>();

    /** POIs inside the area relevant for Ingress. */
    @ManyToMany
    @JoinTable(name = "restricted_area_poi", joinColumns = {
        @JoinColumn(name = "restricted_area")}, inverseJoinColumns = {@JoinColumn(name = "poi")})
    @NotAudited
    private Set<POI> pois = new HashSet<>();

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

    public Set<POI> getPois() {
        return pois;
    }

    public void setPois(Set<POI> pois) {
        this.pois = pois;
    }
}
