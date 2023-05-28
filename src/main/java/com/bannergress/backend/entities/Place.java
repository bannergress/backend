package com.bannergress.backend.entities;

import com.bannergress.backend.enums.PlaceType;
import com.bannergress.backend.utils.PojoBuilder;
import jakarta.persistence.*;
import net.karneim.pojobuilder.GeneratePojoBuilder;
import org.hibernate.annotations.NaturalId;
import org.hibernate.envers.NotAudited;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;

import java.util.*;

/**
 * Represents a Google Maps Place.
 */
@Entity
@Table(name = "place")
@GeneratePojoBuilder(withBuilderInterface = PojoBuilder.class)
public class Place {
    /**
     * Google Maps Place ID.
     */
    @Id
    @Column(name = "id")
    private String id;

    /**
     * Slug (ID which is suitable for use in URLs).
     */
    @NaturalId
    @Column(name = "slug", nullable = false)
    @GenericField
    private String slug;

    /**
     * Type of place (country etc).
     */
    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private PlaceType type;

    @Column(name = "number_of_banners", nullable = false)
    private int numberOfBanners;

    /**
     * Language-specific information.
     */
    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    @IndexedEmbedded
    private List<PlaceInformation> information = new ArrayList<>();

    /**
     * Coordinates this place belongs to.
     */
    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlaceCoordinate> coordinates = new ArrayList<>();

    /**
     * Minimum latitude of the boundary.
     */
    @Column(name = "boundary_min_latitude", nullable = false)
    private Double boundaryMinLatitude;

    /**
     * Minimum longitude of the boundary.
     */
    @Column(name = "boundary_min_longitude", nullable = false)
    private Double boundaryMinLongitude;

    /**
     * Minimum latitude of the boundary.
     */
    @Column(name = "boundary_max_latitude", nullable = false)
    private Double boundaryMaxLatitude;

    /**
     * Minimum longitude of the boundary.
     */
    @Column(name = "boundary_max_longitude", nullable = false)
    private Double boundaryMaxLongitude;

    /**
     * Parent places.
     */
    @ManyToMany
    @JoinTable(name = "place_parenthood", joinColumns = {@JoinColumn(name = "child")}, inverseJoinColumns = {
        @JoinColumn(name = "parent")})
    @NotAudited
    private Set<Place> parentPlaces = new HashSet<>();

    /**
     * Child places.
     */
    @ManyToMany(mappedBy = "parentPlaces")
    private Set<Place> childPlaces = new HashSet<>();

    /**
     * Place is collapsed.
     */
    @Column(name = "collapsed", nullable = false)
    private boolean collapsed;

    /**
     * Banners starting at the place.
     */
    @ManyToMany(mappedBy = "startPlaces")
    private Set<Banner> banners;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public PlaceType getType() {
        return type;
    }

    public void setType(PlaceType type) {
        this.type = type;
    }

    public List<PlaceInformation> getInformation() {
        return information;
    }

    public void setInformation(List<PlaceInformation> information) {
        this.information = information;
    }

    public int getNumberOfBanners() {
        return numberOfBanners;
    }

    public void setNumberOfBanners(int numberOfBanners) {
        this.numberOfBanners = numberOfBanners;
    }

    public List<PlaceCoordinate> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<PlaceCoordinate> coordinates) {
        this.coordinates = coordinates;
    }

    public Double getBoundaryMinLatitude() {
        return boundaryMinLatitude;
    }

    public void setBoundaryMinLatitude(Double boundaryMinLatitude) {
        this.boundaryMinLatitude = boundaryMinLatitude;
    }

    public Double getBoundaryMinLongitude() {
        return boundaryMinLongitude;
    }

    public void setBoundaryMinLongitude(Double boundaryMinLongitude) {
        this.boundaryMinLongitude = boundaryMinLongitude;
    }

    public Double getBoundaryMaxLatitude() {
        return boundaryMaxLatitude;
    }

    public void setBoundaryMaxLatitude(Double boundaryMaxLatitude) {
        this.boundaryMaxLatitude = boundaryMaxLatitude;
    }

    public Double getBoundaryMaxLongitude() {
        return boundaryMaxLongitude;
    }

    public void setBoundaryMaxLongitude(Double boundaryMaxLongitude) {
        this.boundaryMaxLongitude = boundaryMaxLongitude;
    }

    public Set<Place> getParentPlaces() {
        return parentPlaces;
    }

    public Set<Place> getChildPlaces() {
        return childPlaces;
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    }

    public Set<Banner> getBanners() {
        return banners;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Place)) {
            return false;
        }
        Place other = (Place) obj;
        return Objects.equals(id, other.id);
    }
}
