package com.bannergress.backend.entities;

import com.bannergress.backend.enums.MissionStatus;
import com.bannergress.backend.enums.MissionType;
import com.bannergress.backend.utils.PojoBuilder;
import jakarta.persistence.*;
import net.karneim.pojobuilder.GeneratePojoBuilder;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;

import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an Ingress mission.
 */
@Entity
@Table(name = "mission")
@Audited
@AuditTable("mission_audit")
@GeneratePojoBuilder(withBuilderInterface = PojoBuilder.class)
@DynamicUpdate
public class Mission {
    /**
     * Ingress mission ID.
     */
    @Id
    @Column(name = "id")
    @KeywordField
    private String id;

    /**
     * Title.
     */
    @Column(name = "title", nullable = false)
    @FullTextField
    private String title;

    /**
     * Description.
     */
    @Column(name = "description", nullable = true)
    @FullTextField
    private String description;

    /**
     * Picture URL.
     */
    @Column(name = "picture_url", nullable = true)
    private URL picture;

    /**
     * Author.
     */
    @ManyToOne(optional = true)
    @JoinColumn(name = "author")
    @IndexedEmbedded
    private NamedAgent author;

    /**
     * Mission rating in percent.
     */
    @Column(name = "rating", nullable = false)
    @NotAudited
    private double rating;

    /**
     * Number of agents who completed the mission.
     */
    @Column(name = "number_completed", nullable = true)
    @NotAudited
    private Integer numberCompleted;

    /**
     * Average duration of the mission in milliseconds.
     */
    @Column(name = "average_duration_milliseconds", nullable = false)
    @NotAudited
    private long averageDurationMilliseconds;

    /**
     * Type of mission (sequential / any order / hidden).
     */
    @Column(name = "type", nullable = true)
    @Enumerated(EnumType.STRING)
    private MissionType type;

    /**
     * Mission steps.
     */
    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "position")
    private List<MissionStep> steps = new ArrayList<>();

    /**
     * Mission status.
     */
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private MissionStatus status = MissionStatus.disabled;

    /**
     * Timestamp when the mission summary was last updated.
     */
    @Column(name = "latest_update_summary", nullable = true, columnDefinition = "timestamp with time zone")
    @NotAudited
    private Instant latestUpdateSummary;

    /**
     * Timestamp when the mission details were last updated.
     */
    @Column(name = "latest_update_details", nullable = true, columnDefinition = "timestamp with time zone")
    @NotAudited
    private Instant latestUpdateDetails;

    /**
     * Timestamp when the mission status was last updated.
     */
    @Column(name = "latest_update_status", nullable = true, columnDefinition = "timestamp with time zone")
    @NotAudited
    private Instant latestUpdateStatus;

    /**
     * List of banners in which the mission is included.
     */
    @ManyToMany(mappedBy = "missions")
    @NotAudited
    private List<Banner> banners;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public URL getPicture() {
        return picture;
    }

    public void setPicture(URL picture) {
        this.picture = picture;
    }

    public NamedAgent getAuthor() {
        return author;
    }

    public void setAuthor(NamedAgent author) {
        this.author = author;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public Integer getNumberCompleted() {
        return numberCompleted;
    }

    public void setNumberCompleted(Integer numberCompleted) {
        this.numberCompleted = numberCompleted;
    }

    public long getAverageDurationMilliseconds() {
        return averageDurationMilliseconds;
    }

    public void setAverageDurationMilliseconds(long averageDurationMilliseconds) {
        this.averageDurationMilliseconds = averageDurationMilliseconds;
    }

    public MissionType getType() {
        return type;
    }

    public void setType(MissionType type) {
        this.type = type;
    }

    public List<MissionStep> getSteps() {
        return steps;
    }

    public void setSteps(List<MissionStep> steps) {
        this.steps = steps;
    }

    public MissionStatus getStatus() {
        return status;
    }

    public void setStatus(MissionStatus status) {
        this.status = status;
    }

    public Instant getLatestUpdateSummary() {
        return latestUpdateSummary;
    }

    public void setLatestUpdateSummary(Instant latestUpdateSummary) {
        this.latestUpdateSummary = latestUpdateSummary;
    }

    public Instant getLatestUpdateDetails() {
        return latestUpdateDetails;
    }

    public void setLatestUpdateDetails(Instant latestUpdateDetails) {
        this.latestUpdateDetails = latestUpdateDetails;
    }

    public Instant getLatestUpdateStatus() {
        return latestUpdateStatus;
    }

    public void setLatestUpdateStatus(Instant latestUpdateStatus) {
        this.latestUpdateStatus = latestUpdateStatus;
    }

    public List<Banner> getBanners() {
        return banners;
    }

    public void setBanners(List<Banner> banners) {
        this.banners = banners;
    }
}
