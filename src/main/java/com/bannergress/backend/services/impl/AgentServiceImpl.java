package com.bannergress.backend.services.impl;

import com.bannergress.backend.entities.NamedAgent;
import com.bannergress.backend.enums.Faction;
import com.bannergress.backend.services.AgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

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
        namedAgent.setFaction(faction);
        entityManager.persist(namedAgent);
        return namedAgent;
    }
}
