package com.bannergress.backend.services;

import com.bannergress.backend.exceptions.VerificationFailedException;

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
     * @return agent name, corrected for case sensitivity.
     * @throws VerificationFailedException If the control over the agent name could not be established.
     */
    String verify(String agent, UUID verificationToken) throws VerificationFailedException;
}
