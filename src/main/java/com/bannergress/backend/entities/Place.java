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

    /**
     * Language-specific information.
     */
    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlaceInformation> information = new ArrayList<>();

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
}
