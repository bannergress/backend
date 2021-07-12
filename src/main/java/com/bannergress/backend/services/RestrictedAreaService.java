package com.bannergress.backend.services;

import com.bannergress.backend.dto.RestrictedAreaDto;
import com.bannergress.backend.entities.RestrictedArea;

import java.util.Optional;
import java.util.UUID;

/**
 * Service for tasks related to restricted areas.
 */
public interface RestrictedAreaService {
    /**
     * Find restricted area by UUID.
     *
     * @param uuid UUID.
     * @return Restricted area.
     */
    Optional<RestrictedArea> findByUuid(UUID uuid);

    /**
     * Creates a new restricted area.
     *
     * @param restrictedAreaDto Restricted area data.
     * @return UUID of the newly created restricted area.
     */
    UUID create(RestrictedAreaDto restrictedAreaDto);

    /**
     * Updates a restricted area.
     *
     * @param UUID              of the existing restricted area.
     * @param restrictedAreaDto Restricted area data.
     */
    void update(UUID uuid, RestrictedAreaDto restrictedAreaDto);
}
