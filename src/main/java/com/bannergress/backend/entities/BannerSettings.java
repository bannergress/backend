package com.bannergress.backend.entities;

import com.bannergress.backend.enums.BannerListType;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents user settings for a banner.
 */
@Entity
@Table(name = "banner_settings")
@DynamicUpdate
public class BannerSettings {
    /**
     * Internal ID without further meaning.
     */
    @Id
    @Column(name = "uuid", columnDefinition = "uuid")
    @GeneratedValue
    private UUID uuid;

    /**
     * Banner to which the settings apply.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "banner")
    private Banner banner;

    /**
     * User for which the settings apply.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user")
    private User user;

    /**
     * Type of list the banner is on.
     */
    @Column(name = "list_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private BannerListType listType = BannerListType.none;

    /**
     * Timestamp the banner was added to the list in {@link #listType}.
     */
    @Column(name = "list_added", nullable = true)
    private Instant listAdded;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Banner getBanner() {
        return banner;
    }

    public void setBanner(Banner banner) {
        this.banner = banner;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BannerListType getListType() {
        return listType;
    }

    public void setListType(BannerListType listType) {
        this.listType = listType;
    }

    public Instant getListAdded() {
        return listAdded;
    }

    public void setListAdded(Instant listAdded) {
        this.listAdded = listAdded;
    }
}
