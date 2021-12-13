package com.bannergress.backend.services.impl;

import com.bannergress.backend.services.VerificationService;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class TestVerificationServiceImpl {
    @Test
    public void testCompletion() {
        VerificationService verificationService = new CommunityForumVerificationServiceImpl(
            "https://community.ingress.com/en/activity/feed.rss");
        Optional<String> agent = verificationService.verify("someone", UUID.randomUUID());
        assertThat(agent).isEmpty();
    }
}
