package com.bannergress.backend.entities;

import com.bannergress.backend.utils.PojoBuilder;
import net.karneim.pojobuilder.GeneratePojoBuilder;

import javax.persistence.*;

/**
 * Represents translated information for a Google Maps Place.
 */
@Entity
@Table(name = "place_information")
@GeneratePojoBuilder(withBuilderInterface = PojoBuilder.class)
public class PlaceInformation {
    /**
     * Internal ID without meaning.
     */
    @Id
    @Column(name = "id")
    @GeneratedValue
    private long id;

    /**
     * Corresponding Google Maps place.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "place")
    private Place place;

    /**
     * Language code.
     */
    @Column(name = "language_code", nullable = false)
    private String languageCode;

    /**
     * Long name.
     */
    @Column(name = "long_name", nullable = false)
    private String longName;

    /**
     * Short name.
     */
    @Column(name = "short_name", nullable = false)
    private String shortName;

    /**
     * Formatted address.
     */
    @Column(name = "formatted_address", nullable = false)
    private String formattedAddress;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }
}
