package com.bannergress.backend.agent;

/**
 * Service for agent-related tasks.
 */
public interface AgentService {
    /**
     * Imports agent information.
     *
     * @param name    Agent name.
     * @param faction Faction.
     * @return Imported agent information.
     */
    NamedAgent importAgent(String name, Faction faction);
}
