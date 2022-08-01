package com.bannergress.backend.user;

import java.util.UUID;

/**
 * Service for user-related tasks.
 */
public interface UserService {
    /**
     * Gets the current user, and creates it if necessary.
     *
     * @return User.
     */
    public User getOrCreateCurrentUser();

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
     * Attempts to verify an agent name against a token.
     *
     * @param agentName Agent name.
     * @param token     Token.
     * @return <code>true</code> if a verification was completed.
     */
    public boolean attemptVerification(String agentName, UUID token);

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
