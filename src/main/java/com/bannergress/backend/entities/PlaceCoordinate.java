package com.bannergress.backend.entities;

import com.bannergress.backend.utils.PojoBuilder;
import net.karneim.pojobuilder.GeneratePojoBuilder;

import javax.persistence.*;

import java.util.UUID;

/**
 * Represents a pair of coordinates that maps to a specific place.
 */
@Entity
@Table(name = "place_coordinate")
@GeneratePojoBuilder(withBuilderInterface = PojoBuilder.class)
public class PlaceCoordinate {
    /**
     * Internal UUID without meaning.
     */
    @Id
    @Column(name = "uuid", columnDefinition = "uuid")
    @GeneratedValue
    private UUID uuid;

    /**
     * Place the coordinates belong to.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "place")
    private Place place;

    /**
     * Latitude.
     */
    @Column(name = "latitude", nullable = false)
    private double latitude;

    /**
     * Longitude.
     */
    @Column(name = "longitude", nullable = false)
    private double longitude;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
