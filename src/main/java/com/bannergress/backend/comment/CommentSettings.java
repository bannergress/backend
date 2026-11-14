package com.bannergress.backend.comment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Set;

/** Explicit deployment policy; browser input can never choose a comments environment. */
@Component
public record CommentSettings(
    @Value("${comments.enabled:false}") boolean enabled,
    @Value("${comments.environment:PROD}") String environment,
    @Value("${comments.allow-unlinked-test-users:false}") boolean allowUnlinkedTestUsers,
    @Value("${comments.verified-attribute-trusted:false}") boolean verifiedAttributeTrusted,
    @Value("${comments.strike-threshold:3}") int strikeThreshold,
    @Value("${comments.max-per-minute:10}") int maxPerMinute,
    @Value("${comments.max-per-day:100}") int maxPerDay,
    @Value("${comments.moderation-client-id:}") String moderationClientId,
    @Value("${comments.moderator-ids:}") Set<Long> moderatorIds,
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuer) {

    public CommentSettings {
        if (!Set.of("TEST", "PROD").contains(environment) || strikeThreshold < 1
            || maxPerMinute < 1 || maxPerDay < 1) {
            throw new IllegalArgumentException("Invalid comments configuration");
        }
        String realm = URI.create(issuer).getPath();
        boolean testRealm = realm != null && realm.endsWith("/realms/bannergress-test");
        if (((enabled || allowUnlinkedTestUsers) && (environment.equals("TEST") != testRealm))
            || (allowUnlinkedTestUsers && !environment.equals("TEST"))) {
            throw new IllegalArgumentException("Comments environment must match the trusted identity realm");
        }
        moderatorIds = moderatorIds == null ? Set.of() : moderatorIds.stream().filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    public String website() {
        return environment.equals("TEST") ? "https://test.bannergress.com" : "https://bannergress.com";
    }
}
