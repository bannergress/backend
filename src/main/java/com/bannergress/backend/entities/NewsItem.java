package com.bannergress.backend.entities;

import com.bannergress.backend.utils.PojoBuilder;
import net.karneim.pojobuilder.GeneratePojoBuilder;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;

import java.time.Instant;

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
     * Internal ID without further meaning.
     */
    @Id
    @Column(name = "id")
    @GeneratedValue
    private long id;

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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
