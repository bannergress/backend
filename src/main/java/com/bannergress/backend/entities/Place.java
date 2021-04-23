package com.bannergress.backend.entities;

import com.bannergress.backend.enums.PlaceType;
import com.bannergress.backend.utils.PojoBuilder;
import net.karneim.pojobuilder.GeneratePojoBuilder;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

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

    @ManyToOne(optional = true)
    @JoinColumn(name = "parent_place")
    private Place parentPlace;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Place getParentPlace() {
        return parentPlace;
    }

    public void setParentPlace(Place parentPlace) {
        this.parentPlace = parentPlace;
    }
}
