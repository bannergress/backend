package com.bannergress.backend.entities;

import com.bannergress.backend.utils.PojoBuilder;
import net.karneim.pojobuilder.GeneratePojoBuilder;
import org.locationtech.jts.geom.Point;

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
     * Point.
     */
    @Basic
    @Column(name = "point", nullable = false)
    private Point point;

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

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }
}
