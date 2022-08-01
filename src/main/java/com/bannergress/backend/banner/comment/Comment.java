package com.bannergress.backend.banner.comment;

import com.bannergress.backend.banner.Banner;
import com.bannergress.backend.user.User;
import jakarta.persistence.*;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Represents a user comment or review. */
@Entity
@Table(name = "comment")
@Audited
@AuditTable("comment_audit")
public class Comment {
    /** Internal ID without further meaning. */
    @Id
    @Column(name = "uuid")
    private UUID uuid;

    /** Banner to which the comment applies. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "banner")
    private Banner banner;

    /** User who created the comment. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private User user;

    /** Type of comment (comment or review). */
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private CommentType type;

    /** Timestamp the comment was created. */
    @Column(name = "created")
    @NotAudited
    private Instant created;

    /** Text of the comment. */
    @Column(name = "comment")
    private String comment;

    /** Always (24/7) accessible. */
    @Column(name = "rating_round_the_clock")
    @Enumerated(EnumType.STRING)
    private RoundTheClockType ratingRoundTheClock;

    /** Overall rating (1 to 5 stars). */
    @Column(name = "rating_overall")
    private Integer ratingOverall;

    /** Accessibility rating (1 to 5 stars). */
    @Column(name = "rating_accessibility")
    private Integer ratingAccessibility;

    /** Passphrase rating (1 to 5 stars). */
    @Column(name = "rating_passphrases")
    private Integer ratingPassphrases;

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

    public CommentType getType() {
        return type;
    }

    public void setType(CommentType type) {
        this.type = type;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public RoundTheClockType getRatingRoundTheClock() {
        return ratingRoundTheClock;
    }

    public void setRatingRoundTheClock(RoundTheClockType ratingRoundTheClock) {
        this.ratingRoundTheClock = ratingRoundTheClock;
    }

    public Integer getRatingOverall() {
        return ratingOverall;
    }

    public void setRatingOverall(Integer ratingOverall) {
        this.ratingOverall = ratingOverall;
    }

    public Integer getRatingAccessibility() {
        return ratingAccessibility;
    }

    public void setRatingAccessibility(Integer ratingAccessibility) {
        this.ratingAccessibility = ratingAccessibility;
    }

    public Integer getRatingPassphrases() {
        return ratingPassphrases;
    }

    public void setRatingPassphrases(Integer ratingPassphrases) {
        this.ratingPassphrases = ratingPassphrases;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Comment)) {
            return false;
        }
        return Objects.equals(uuid, ((Comment) obj).uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
