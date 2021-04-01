package com.bannergress.backend.entities;

import com.bannergress.backend.utils.PojoBuilder;
import net.karneim.pojobuilder.GeneratePojoBuilder;

import javax.persistence.*;

/**
 * Represents a banner picture.
 */
@Entity
@Table(name = "banner_picture")
@GeneratePojoBuilder(withBuilderInterface = PojoBuilder.class)
public class BannerPicture {
    /**
     * Hash of the picture.
     */
    @Id
    @Column(name = "hash")
    @Access(AccessType.PROPERTY)
    private String hash;

    @Lob
    @Column(name = "picture", nullable = false)
    private byte[] picture;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public byte[] getPicture() {
        return picture;
    }

    public void setPicture(byte[] picture) {
        this.picture = picture;
    }
}
