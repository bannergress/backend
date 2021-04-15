package com.bannergress.backend.entities;

import com.bannergress.backend.utils.PojoBuilder;
import net.karneim.pojobuilder.GeneratePojoBuilder;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a single news item.
 */
@Entity
@Table(name = "news")
@Audited
@AuditTable("news_audit")
@GeneratePojoBuilder(withBuilderInterface = PojoBuilder.class)
public class NewsItem {
    /**
     * Internal UUID without further meaning.
     */
    @Id
    @Column(name = "uuid", columnDefinition = "uuid")
    @GeneratedValue
    private UUID uuid;

    /**
     * Content.
     */
    @Column(name = "content", nullable = false)
    private String content;

    /**
     * Timestamp the news item was created.
     */
    @Column(name = "created", nullable = false)
    @NotAudited
    private Instant created;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }
}
