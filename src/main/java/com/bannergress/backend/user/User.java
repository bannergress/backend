package com.bannergress.backend.user;

import com.bannergress.backend.banner.settings.BannerSettings;
import com.bannergress.backend.utils.PojoBuilder;
import jakarta.persistence.*;
import net.karneim.pojobuilder.GeneratePojoBuilder;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.search.engine.backend.types.Searchable;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;

import java.util.List;
import java.util.UUID;

/**
 * Represents a user.
 */
@Entity
@Table(name = "user")
@GeneratePojoBuilder(withBuilderInterface = PojoBuilder.class)
@DynamicUpdate
public class User {
    /**
     * Keycloak user ID.
     */
    @Id
    @Column(name = "id")
    @GenericField(searchable = Searchable.YES, sortable = Sortable.NO)
    private String id;

    /**
     * Claimed agent name.
     */
    @Column(name = "verification_agent", nullable = true)
    private String verificationAgent;

    /**
     * Verification token.
     */
    @Column(name = "verification_token", columnDefinition = "uuid", nullable = true)
    private UUID verificationToken;

    /**
     * List of banner-specific settings for the user.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<BannerSettings> settings;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVerificationAgent() {
        return verificationAgent;
    }

    public void setVerificationAgent(String verificationAgent) {
        this.verificationAgent = verificationAgent;
    }

    public UUID getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(UUID verificationToken) {
        this.verificationToken = verificationToken;
    }
}
