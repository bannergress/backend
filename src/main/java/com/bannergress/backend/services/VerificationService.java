package com.bannergress.backend.services;

import java.util.Optional;
import java.util.UUID;

/**
 * Service for agent verification.
 */
public interface VerificationService {
    /**
     * Tries to verify control over an agent name.
     *
     * @param agent             Agent name to verify control.
     * @param verificationToken Token for agent verification.
     * @return If the verification was successful: agent name, corrected for case sensitivity.
     */
    Optional<String> verify(String agent, UUID verificationToken);
}
