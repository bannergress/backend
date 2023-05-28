package com.bannergress.backend.entities;

import com.bannergress.backend.utils.PojoBuilder;
import jakarta.persistence.*;
import net.karneim.pojobuilder.GeneratePojoBuilder;

import java.time.Instant;
import java.util.List;

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

    @Column(name = "picture", nullable = false)
    private byte[] picture;

    @Column(name = "expiration", nullable = true)
    private Instant expiration;

    @OneToMany(mappedBy = "picture", fetch = FetchType.LAZY)
    private List<Banner> banners;

    public Instant getExpiration() {
        return expiration;
    }

    public void setExpiration(Instant expiration) {
        this.expiration = expiration;
    }

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

    public List<Banner> getBanners() {
        return banners;
    }

    public void setBanners(List<Banner> banners) {
        this.banners = banners;
    }
}
