package com.bannergress.backend.banner;

import com.bannergress.backend.banner.comment.Comment;
import com.bannergress.backend.banner.picture.BannerPicture;
import com.bannergress.backend.banner.settings.BannerSettings;
import com.bannergress.backend.mission.Mission;
import com.bannergress.backend.place.Place;
import com.bannergress.backend.spatial.PointBridge;
import com.bannergress.backend.utils.PojoBuilder;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Maps;
import jakarta.persistence.*;
import net.karneim.pojobuilder.GeneratePojoBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.SortNatural;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.search.engine.backend.types.ObjectStructure;
import org.hibernate.search.engine.backend.types.Searchable;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.bridge.mapping.annotation.ValueBridgeRef;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

/**
 * Represents a banner.
 */
@Entity
@Table(name = "banner")
@Audited
@GeneratePojoBuilder(withBuilderInterface = PojoBuilder.class)
@Indexed
public class Banner {
    /**
     * Internal ID without further meaning.
     */
    @Id
    @Column(name = "uuid", columnDefinition = "uuid")
    @GeneratedValue
    @GenericField(searchable = Searchable.NO, sortable = Sortable.YES)
    private UUID uuid;

    /**
     * Canonical slug (ID which is suitable for use in URLs).
     */
    @NaturalId(mutable = true)
    @Column(name = "canonical_slug", nullable = false)
    @NotAudited
    private String canonicalSlug;

    /**
     * All slugs, including the {@link #canonicalSlug}.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "banner_slug", joinColumns = @JoinColumn(name = "banner"))
    @Column(name = "slug", nullable = false)
    @NotAudited
    private Set<String> slugs = new HashSet<>();

    /**
     * Title.
     */
    @Column(name = "title", nullable = false)
    @FullTextField
    @GenericField(name = "titleSort", searchable = Searchable.NO, sortable = Sortable.YES)
    private String title;

    /**
     * Description.
     */
    @Column(name = "description", nullable = true)
    @FullTextField
    private String description;

    /** Width of the banner in missions. */
    @Column(name = "width", nullable = false)
    private int width;

    /**
     * Number of missions.
     */
    @Column(name = "number_of_missions", nullable = false)
    @NotAudited
    @GenericField(searchable = Searchable.NO, sortable = Sortable.YES)
    private int numberOfMissions;

    /**
     * Number of submitted ("not yet published") missions.
     */
    @Column(name = "number_of_submitted_missions", nullable = false)
    @NotAudited
    private int numberOfSubmittedMissions;

    /**
     * Number of disabled ("not published anymore") missions.
     */
    @Column(name = "number_of_disabled_missions", nullable = false)
    @NotAudited
    private int numberOfDisabledMissions;

    /**
     * Map between the zero-based mission position and the mission. The mission
     * position must be less than {@link #numberOfMissions}. The map may be sparse,
     * i.e. not every position is necessarily mapped to a mission.
     */
    @ManyToMany
    @JoinTable(name = "banner_mission", joinColumns = {@JoinColumn(name = "banner")}, inverseJoinColumns = {
        @JoinColumn(name = "mission")})
    @MapKeyColumn(name = "position")
    @SortNatural
    @IndexedEmbedded
    private SortedMap<Integer, Mission> missions = new TreeMap<>();

    /**
     * Set of zero-based mission positions where a mission is supposed to be. This set and the keyset of {@link #missions} are mutually exclusive.
     */
    @ElementCollection
    @CollectionTable(name = "banner_placeholder", joinColumns = {@JoinColumn(name = "banner")})
    @Column(name = "position")
    @SortNatural
    private SortedSet<Integer> placeholders = new TreeSet<>();

    /**
     * Start portal of the first mission.
     */
    @Basic
    @Column(name = "start_point", nullable = true)
    @NotAudited
    @GenericField(searchable = Searchable.YES, sortable = Sortable.YES, valueBridge = @ValueBridgeRef(type = PointBridge.class))
    private Point startPoint;

    /**
     * Length in meters.
     */
    @Column(name = "length_meters", nullable = true)
    @NotAudited
    @GenericField(searchable = Searchable.NO, sortable = Sortable.YES)
    private Integer lengthMeters;

    /**
     * All missions are online.
     */
    @Column(name = "online", nullable = false)
    @NotAudited
    @GenericField(searchable = Searchable.YES, sortable = Sortable.NO)
    private boolean online;

    /**
     * Generated Picture.
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
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
    @IndexedEmbedded
    private Set<Place> startPlaces = new HashSet<>();

    /**
     * Timestamp the banner was created.
     */
    @Column(name = "created", nullable = false)
    @NotAudited
    @GenericField(searchable = Searchable.NO, sortable = Sortable.YES)
    private Instant created;

    /**
     * Type of banner (sequential or any order).
     */
    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private BannerType type;

    /**
     * List of user-specific settings for the banner.
     */
    @OneToMany(mappedBy = "banner", cascade = CascadeType.ALL)
    @NotAudited
    @IndexedEmbedded(structure = ObjectStructure.NESTED)
    private List<BannerSettings> settings;

    /**
     * List of comments for the banner.
     */
    @OneToMany(mappedBy = "banner", cascade = CascadeType.ALL)
    private Set<Comment> comments = new HashSet<>();

    /**
     * Warning text.
     */
    @Column(name = "warning", nullable = true)
    private String warning;

    /**
     * Planned date where missions are expected to be put offline.
     */
    @Column(name = "planned_offline_date", nullable = true)
    private LocalDate plannedOfflineDate;

    /**
     * Start date for the corresponding event (inclusive).
     */
    @Column(name = "event_start_date", nullable = true)
    private LocalDate eventStartDate;

    /**
     * End date for the corresponding event (inclusive).
     */
    @Column(name = "event_end_date", nullable = true)
    private LocalDate eventEndDate;

    /**
     * Start timestamp for the corresponding event (inclusive).
     */
    @Column(name = "event_start_timestamp", nullable = true)
    @NotAudited
    @GenericField(searchable = Searchable.YES, sortable = Sortable.NO)
    private Instant eventStartTimestamp;

    /**
     * End timestamp for the corresponding event (exclusive).
     */
    @Column(name = "event_end_timestamp", nullable = true)
    @NotAudited
    @GenericField(searchable = Searchable.YES, sortable = Sortable.NO)
    private Instant eventEndTimestamp;

    /** Always (24/7) accessible. */
    @Column(name = "round_the_clock")
    private Boolean roundTheClock;

    /** Average of always (24/7) accessible. Range between 0 (all false) and 1 (all true). */
    @Column(name = "average_rating_round_the_clock")
    @NotAudited
    private Float averageRatingRoundTheClock;

    /** Average overall rating (1 to 5 stars). */
    @Column(name = "average_rating_overall")
    @NotAudited
    private Float averageRatingOverall;

    /** Average accessibility rating (1 to 5 stars). */
    @Column(name = "average_rating_accessibility")
    @NotAudited
    private Float averageRatingAccessibility;

    /** Average passphrase rating (1 to 5 stars). */
    @Column(name = "average_rating_passphrases")
    @NotAudited
    private Float averageRatingPassphrases;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getCanonicalSlug() {
        return canonicalSlug;
    }

    public void setCanonicalSlug(String canonicalSlug) {
        this.canonicalSlug = canonicalSlug;
    }

    public Set<String> getSlugs() {
        return slugs;
    }

    public void setSlugs(Set<String> slugs) {
        this.slugs = slugs;
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

    public int getNumberOfSubmittedMissions() {
        return numberOfSubmittedMissions;
    }

    public void setNumberOfSubmittedMissions(int numberOfSubmittedMissions) {
        this.numberOfSubmittedMissions = numberOfSubmittedMissions;
    }

    public int getNumberOfDisabledMissions() {
        return numberOfDisabledMissions;
    }

    public void setNumberOfDisabledMissions(int numberOfDisabledMissions) {
        this.numberOfDisabledMissions = numberOfDisabledMissions;
    }

    public SortedMap<Integer, Mission> getMissions() {
        return missions;
    }

    public void setMissions(SortedMap<Integer, Mission> missions) {
        this.missions = missions;
    }

    public SortedSet<Integer> getPlaceholders() {
        return placeholders;
    }

    public void setPlaceholders(SortedSet<Integer> placeholders) {
        this.placeholders = placeholders;
    }

    public ImmutableSortedMap<Integer, Optional<Mission>> getMissionsAndPlaceholders() {
        return ImmutableSortedMap.<Integer, Optional<Mission>>naturalOrder()
            .putAll(Maps.transformValues(missions, Optional::of))
            .putAll(Maps.asMap(placeholders, p -> Optional.empty())).build();
    }

    public Point getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(Point startPoint) {
        this.startPoint = startPoint;
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

    public LocalDate getPlannedOfflineDate() {
        return plannedOfflineDate;
    }

    public void setPlannedOfflineDate(LocalDate plannedOfflineDate) {
        this.plannedOfflineDate = plannedOfflineDate;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    public String getWarning() {
        return warning;
    }

    public void setEventStartDate(LocalDate eventStartDate) {
        this.eventStartDate = eventStartDate;
    }

    public LocalDate getEventStartDate() {
        return eventStartDate;
    }

    public void setEventEndDate(LocalDate eventEndDate) {
        this.eventEndDate = eventEndDate;
    }

    public LocalDate getEventEndDate() {
        return eventEndDate;
    }

    public void setEventStartTimestamp(Instant eventStartTimestamp) {
        this.eventStartTimestamp = eventStartTimestamp;
    }

    public Instant getEventStartTimestamp() {
        return eventStartTimestamp;
    }

    public void setEventEndTimestamp(Instant eventEndTimestamp) {
        this.eventEndTimestamp = eventEndTimestamp;
    }

    public Instant getEventEndTimestamp() {
        return eventEndTimestamp;
    }

    public Set<Comment> getComments() {
        return comments;
    }

    public void setComments(Set<Comment> comments) {
        this.comments = comments;
    }

    public Boolean getRoundTheClock() {
        return roundTheClock;
    }

    public void setRoundTheClock(Boolean roundTheClock) {
        this.roundTheClock = roundTheClock;
    }

    public Float getAverageRatingRoundTheClock() {
        return averageRatingRoundTheClock;
    }

    public void setAverageRatingRoundTheClock(Float averageRatingRoundTheClock) {
        this.averageRatingRoundTheClock = averageRatingRoundTheClock;
    }

    public Float getAverageRatingOverall() {
        return averageRatingOverall;
    }

    public void setAverageRatingOverall(Float averageRatingOverall) {
        this.averageRatingOverall = averageRatingOverall;
    }

    public Float getAverageRatingAccessibility() {
        return averageRatingAccessibility;
    }

    public void setAverageRatingAccessibility(Float averageRatingAccessibility) {
        this.averageRatingAccessibility = averageRatingAccessibility;
    }

    public Float getAverageRatingPassphrases() {
        return averageRatingPassphrases;
    }

    public void setAverageRatingPassphrases(Float averageRatingPassphrases) {
        this.averageRatingPassphrases = averageRatingPassphrases;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Banner)) {
            return false;
        }
        Banner other = (Banner) obj;
        return Objects.equals(uuid, other.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
