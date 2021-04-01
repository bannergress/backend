package com.bannergress.backend.entities;

import com.bannergress.backend.enums.POIType;
import com.bannergress.backend.utils.PojoBuilder;
import net.karneim.pojobuilder.GeneratePojoBuilder;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

import javax.persistence.*;

import java.net.URL;

/**
 * Represents a point of interest, i.e. a portal or a field trip waypoint.
 */
@Entity
@Table(name = "poi")
@Audited
@AuditTable("poi_audit")
@GeneratePojoBuilder(withBuilderInterface = PojoBuilder.class)
public class POI {
    /**
     * Ingress POI ID.
     */
    @Id
    @Column(name = "id")
    private String id;

    /**
     * Title.
     */
    @Column(name = "title", nullable = true)
    private String title;

    /**
     * Latitude.
     */
    @Column(name = "latitude", nullable = true)
    private Double latitude;

    /**
     * Longitude.
     */
    @Column(name = "longitude", nullable = true)
    private Double longitude;

    /**
     * Picture URL.
     */
    @Column(name = "picture_url", nullable = true)
    private URL picture;

    /**
     * Type of the POI (portal or field trip waypoint.
     */
    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private POIType type;

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

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public URL getPicture() {
        return picture;
    }

    public void setPicture(URL picture) {
        this.picture = picture;
    }

    public POIType getType() {
        return type;
    }

    public void setType(POIType type) {
        this.type = type;
    }
}
