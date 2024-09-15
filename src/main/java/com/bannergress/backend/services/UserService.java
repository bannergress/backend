package com.bannergress.backend.services;

import com.bannergress.backend.entities.User;

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
