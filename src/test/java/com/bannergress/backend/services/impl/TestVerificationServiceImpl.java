package com.bannergress.backend.services.impl;

import com.bannergress.backend.exceptions.VerificationFailedException;
import com.bannergress.backend.services.VerificationService;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class TestVerificationServiceImpl {
    @Test
    public void testCompletion() {
        VerificationService verificationService = new CommunityForumVerificationServiceImpl(
            "https://community.ingress.com/en/activity/feed.rss");
        assertThatExceptionOfType(VerificationFailedException.class)
            .isThrownBy(() -> verificationService.verify("someone", UUID.randomUUID()));
    }
}
