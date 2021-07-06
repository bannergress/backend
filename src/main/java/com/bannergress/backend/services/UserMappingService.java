package com.bannergress.backend.services;

import java.util.Optional;

/**
 * Service for mapping between user ID and agent name.
 */
public interface UserMappingService {
    /**
     * Sets the agent name of a user.
     *
     * @param user  User for which to set the agent name.
     * @param agent Agent name to set.
     */
    public void setAgentName(String user, String agent);

    /**
     * Gets the agent name of a user.
     *
     * @param user User for which to get the agent name.
     * @return Agent name, if available.
     */
    public Optional<String> getAgentName(String user);
}
