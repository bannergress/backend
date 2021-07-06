package com.bannergress.backend.entities;

import com.bannergress.backend.utils.PojoBuilder;
import net.karneim.pojobuilder.GeneratePojoBuilder;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
