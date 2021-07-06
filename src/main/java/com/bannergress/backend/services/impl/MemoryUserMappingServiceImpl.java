package com.bannergress.backend.services.impl;

import com.bannergress.backend.services.UserMappingService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * User mapping service that stores information in-memory.<br>
 * Only suitable for development.
 */
@Service
@Profile("dev")
public class MemoryUserMappingServiceImpl implements UserMappingService {
    private final Map<String, String> mapping = new HashMap<>();

    @Override
    public void setAgentName(String user, String agent) {
        mapping.put(user, agent);

    }

    @Override
    public Optional<String> getAgentName(String user) {
        return Optional.ofNullable(mapping.get(user));
    }
}
