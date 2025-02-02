package com.bannergress.backend.poi;

import com.bannergress.backend.utils.PojoBuilder;
import jakarta.persistence.*;
import net.karneim.pojobuilder.GeneratePojoBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.envers.Audited;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.net.URL;

/**
 * Represents a point of interest, i.e. a portal or a field trip waypoint.
 */
@Entity
@Table(name = "poi")
@Audited
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
     * Point.
     */
    @Basic
    @Column(name = "point", nullable = true)
    private Point point;

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
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
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

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
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
