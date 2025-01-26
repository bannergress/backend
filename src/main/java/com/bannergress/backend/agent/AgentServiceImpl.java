package com.bannergress.backend.agent;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link AgentService}.
 */
@Service
@Transactional
public class AgentServiceImpl implements AgentService {
    @Autowired
    private EntityManager entityManager;

    public NamedAgent importAgent(String name, Faction faction) {
        NamedAgent namedAgent = entityManager.find(NamedAgent.class, name);
        if (namedAgent == null) {
            namedAgent = new NamedAgent();
            namedAgent.setName(name);
        }
        if (faction != null) {
            namedAgent.setFaction(faction);
        }
        entityManager.persist(namedAgent);
        return namedAgent;
    }
}
