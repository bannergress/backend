package com.bannergress.backend.entities;

import com.bannergress.backend.enums.BannerType;
import com.bannergress.backend.utils.PojoBuilder;
import net.karneim.pojobuilder.GeneratePojoBuilder;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.SortNatural;
import org.hibernate.envers.AuditJoinTable;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;

import java.time.Instant;
import java.util.*;

/**
 * Represents a banner.
 */
@Entity
@Table(name = "banner")
@Audited
@AuditTable("banner_audit")
@GeneratePojoBuilder(withBuilderInterface = PojoBuilder.class)
public class Banner {
    /**
     * Internal ID without further meaning.
     */
    @Id
    @Column(name = "uuid", columnDefinition = "uuid")
    @GeneratedValue
    private UUID uuid;

    /**
     * Slug (ID which is suitable for use in URLs).
     */
    @NaturalId
    @Column(name = "slug", nullable = false)
    @NotAudited
    private String slug;

    /**
     * Title.
     */
    @Column(name = "title", nullable = false)
    private String title;

    /**
     * Description.
     */
    @Column(name = "description", nullable = true)
    private String description;

    /** Width of the banner in missions. */
    @Column(name = "width", nullable = false)
    private int width;

    /**
     * Number of missions.
     */
    @Column(name = "number_of_missions", nullable = false)
    @NotAudited
    private int numberOfMissions;

    /**
     * Map between the zero-based mission position and the mission. The mission
     * position must be less than {@link #numberOfMissions}. The map may be sparse,
     * i.e. not every position is necessarily mapped to a mission.
     */
    @ManyToMany
    @JoinTable(name = "banner_mission", joinColumns = {@JoinColumn(name = "banner")}, inverseJoinColumns = {
        @JoinColumn(name = "mission")})
    @MapKeyColumn(name = "position")
    @AuditJoinTable(name = "banner_mission_audit")
    @SortNatural
    private SortedMap<Integer, Mission> missions = new TreeMap<>();

    /**
     * Latitude of the start portal of the first mission.
     */
    @Column(name = "start_latitude", nullable = true)
    @NotAudited
    private Double startLatitude;

    /**
     * Longitude of the start portal of the first mission.
     */
    @Column(name = "start_longitude", nullable = true)
    @NotAudited
    private Double startLongitude;

    /**
     * Length in meters.
     */
    @Column(name = "length_meters", nullable = true)
    @NotAudited
    private Integer lengthMeters;

    /**
     * All mission information is present.
     */
    @Column(name = "complete", nullable = false)
    @NotAudited
    private boolean complete;

    /**
     * All missions are online.
     */
    @Column(name = "online", nullable = false)
    @NotAudited
    private boolean online;

    /**
     * Generated Picture.
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "picture")
    @NotAudited
    private BannerPicture picture;

    /**
     * Places of the start portal of the first mission. The list contains multiple
     * places with different types.
     */
    @ManyToMany
    @JoinTable(name = "banner_start_place", joinColumns = {@JoinColumn(name = "banner")}, inverseJoinColumns = {
        @JoinColumn(name = "place")})
    @NotAudited
    private Set<Place> startPlaces = new HashSet<>();

    /**
     * Timestamp the banner was created.
     */
    @Column(name = "created", nullable = false)
    @NotAudited
    private Instant created;

    /**
     * Type of banner (sequential or any order).
     */
    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private BannerType type;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getNumberOfMissions() {
        return numberOfMissions;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setNumberOfMissions(int numberOfMissions) {
        this.numberOfMissions = numberOfMissions;
    }

    public SortedMap<Integer, Mission> getMissions() {
        return missions;
    }

    public void setMissions(SortedMap<Integer, Mission> missions) {
        this.missions = missions;
    }

    public Double getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(Double startLatitude) {
        this.startLatitude = startLatitude;
    }

    public Double getStartLongitude() {
        return startLongitude;
    }

    public void setStartLongitude(Double startLongitude) {
        this.startLongitude = startLongitude;
    }

    public Integer getLengthMeters() {
        return lengthMeters;
    }

    public void setLengthMeters(Integer lengthMeters) {
        this.lengthMeters = lengthMeters;
    }

    public Set<Place> getStartPlaces() {
        return startPlaces;
    }

    public void setStartPlaces(Set<Place> startPlaces) {
        this.startPlaces = startPlaces;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public BannerPicture getPicture() {
        return picture;
    }

    public void setPicture(BannerPicture picture) {
        this.picture = picture;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public BannerType getType() {
        return type;
    }

    public void setType(BannerType type) {
        this.type = type;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Banner banner = (Banner) o;
        return Objects.equals(uuid, banner.uuid) && numberOfMissions == banner.numberOfMissions
            && complete == banner.complete && online == banner.online && Objects.equals(title, banner.title)
            && Objects.equals(description, banner.description) && Objects.equals(missions, banner.missions)
            && Objects.equals(startLatitude, banner.startLatitude)
            && Objects.equals(startLongitude, banner.startLongitude)
            && Objects.equals(lengthMeters, banner.lengthMeters) && Objects.equals(picture, banner.picture)
            && Objects.equals(startPlaces, banner.startPlaces) && Objects.equals(created, banner.created)
            && type == banner.type && Objects.equals(slug, banner.slug);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, title, description, numberOfMissions, missions, startLatitude, startLongitude,
            lengthMeters, complete, online, picture, startPlaces, created, type, slug);
    }
}
