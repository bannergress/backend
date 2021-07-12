package com.bannergress.backend.entities;

import jakarta.persistence.*;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.locationtech.jts.geom.MultiPolygon;

import java.net.URL;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a restricted area.
 */
@Entity
@Table(name = "restricted_area")
@Audited
@AuditTable("restricted_area_audit")
public class RestrictedArea {
    /**
     * Internal ID without further meaning.
     */
    @Id
    @Column(name = "uuid", columnDefinition = "uuid")
    @GeneratedValue
    private UUID uuid;

    /**
     * Title.
     */
    @Column(name = "title", nullable = false)
    private String title;

    /**
     * Area.
     */
    @Column(name = "area", nullable = false)
    private MultiPolygon area;

    /**
     * Is the access generally restricted?
     */
    @Column(name = "general_restriction", nullable = false)
    private boolean generalRestriction;

    /**
     * Description of the general access restriction.
     */
    @Column(name = "general_restriction_description", nullable = true)
    private String generalRestrictionDescription;

    /**
     * Additional URL for the general access restriction.
     */
    @Column(name = "general_restriction_url", nullable = true)
    private URL generalRestrictionUrl;

    /**
     * Is the access restricted at certain times?
     */
    @Column(name = "timed_restriction", nullable = false)
    private boolean timedRestriction;

    /**
     * Description of the timed access restriction.
     */
    @Column(name = "timed_restriction_description", nullable = true)
    private String timedRestrictionDescription;

    /**
     * Additional URL for the timed access restriction.
     */
    @Column(name = "timed_restriction_url", nullable = true)
    private URL timedRestrictionUrl;

    /**
     * Is the access restricted to paying customers?
     */
    @Column(name = "monetary_restriction", nullable = false)
    private boolean monetaryRestriction;

    /**
     * Description of the monetary access restriction.
     */
    @Column(name = "general_restriction_description", nullable = true)
    private String monetaryRestrictionDescription;

    /**
     * Additional URL for the monetary access restriction.
     */
    @Column(name = "general_restriction_url", nullable = true)
    private URL monetaryRestrictionUrl;

    /**
     * Start date and time of the restriction.
     */
    @Column(name = "start_date", nullable = true)
    private Instant startDate;

    /**
     * End date and time of the restriction.
     */
    @Column(name = "end_date", nullable = true)
    private Instant endDate;

    /**
     * Set of POIs contained in the area.
     */
    @ManyToMany
    @JoinTable(name = "restricted_area_poi", joinColumns = {
        @JoinColumn(name = "restricted_area")}, inverseJoinColumns = {@JoinColumn(name = "poi")})
    @NotAudited
    private Set<POI> pois;

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

    public MultiPolygon getArea() {
        return area;
    }

    public void setArea(MultiPolygon area) {
        this.area = area;
    }

    public boolean isGeneralRestriction() {
        return generalRestriction;
    }

    public void setGeneralRestriction(boolean generalRestriction) {
        this.generalRestriction = generalRestriction;
    }

    public String getGeneralRestrictionDescription() {
        return generalRestrictionDescription;
    }

    public void setGeneralRestrictionDescription(String generalRestrictionDescription) {
        this.generalRestrictionDescription = generalRestrictionDescription;
    }

    public URL getGeneralRestrictionUrl() {
        return generalRestrictionUrl;
    }

    public void setGeneralRestrictionUrl(URL generalRestrictionUrl) {
        this.generalRestrictionUrl = generalRestrictionUrl;
    }

    public boolean isTimedRestriction() {
        return timedRestriction;
    }

    public void setTimedRestriction(boolean timedRestriction) {
        this.timedRestriction = timedRestriction;
    }

    public String getTimedRestrictionDescription() {
        return timedRestrictionDescription;
    }

    public void setTimedRestrictionDescription(String timedRestrictionDescription) {
        this.timedRestrictionDescription = timedRestrictionDescription;
    }

    public URL getTimedRestrictionUrl() {
        return timedRestrictionUrl;
    }

    public void setTimedRestrictionUrl(URL timedRestrictionUrl) {
        this.timedRestrictionUrl = timedRestrictionUrl;
    }

    public boolean isMonetaryRestriction() {
        return monetaryRestriction;
    }

    public void setMonetaryRestriction(boolean monetaryRestriction) {
        this.monetaryRestriction = monetaryRestriction;
    }

    public String getMonetaryRestrictionDescription() {
        return monetaryRestrictionDescription;
    }

    public void setMonetaryRestrictionDescription(String monetaryRestrictionDescription) {
        this.monetaryRestrictionDescription = monetaryRestrictionDescription;
    }

    public URL getMonetaryRestrictionUrl() {
        return monetaryRestrictionUrl;
    }

    public void setMonetaryRestrictionUrl(URL monetaryRestrictionUrl) {
        this.monetaryRestrictionUrl = monetaryRestrictionUrl;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public Set<POI> getPois() {
        return pois;
    }

    public void setPois(Set<POI> pois) {
        this.pois = pois;
    }
}
