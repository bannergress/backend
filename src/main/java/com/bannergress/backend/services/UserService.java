package com.bannergress.backend.services;

import com.bannergress.backend.entities.User;
import com.bannergress.backend.exceptions.VerificationStateException;

import java.util.Optional;

/**
 * Service for user-related tasks.
 */
public interface UserService {
    /**
     * Gets a user for a user ID, and creates it if necessary.
     *
     * @param userId User ID.
     * @return User.
     */
    public User getOrCreate(String userId);

    /**
     * Adds a claim for an agent name to a user ID.
     *
     * @param userId User ID.
     * @param agent  Agent name.
     */
    void claim(String userId, String agent);

    /**
     * Verifies a user.
     *
     * @param userId user to verify.
     * @return If the verification was successful: agent name, corrected for case sensitivity.
     * @throws VerificationStateException If the verification process hasn't been started.
     */
    public Optional<String> verify(String userId) throws VerificationStateException;

    /**
     * Unlinks a user from an agent name.
     *
     * @param userId user to unlink.
     */
    public void unlink(String userId);

    /**
     * Clears a claim for an agent name to a user ID.
     *
     * @param userId User ID.
     */
    public void clearClaim(String userId);
}
