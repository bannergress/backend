package com.bannergress.backend.services;

import com.bannergress.backend.dto.CreatorGetMissionForProfile;
import com.bannergress.backend.dto.CreatorGetMissionsList;
import jakarta.validation.Valid;

/**
 * Service for imports from mission creator.
 */
public interface CreatorImportService {
    /**
     * Imports a single mission from creator-based data.
     *
     * @param data Creator-based data.
     */
    void importGetMissionForProfile(@Valid CreatorGetMissionForProfile data);

    /**
     * Imports a a list of user missions from creator-based data.
     *
     * @param data   Creator-based data.
     */
    void importGetMissionsList(@Valid CreatorGetMissionsList data);
}
