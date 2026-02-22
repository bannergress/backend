package com.bannergress.backend.news;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

/**
 * Transports information about a single news item.
 */
record NewsItemDto(UUID uuid, @NotNull String content, Instant created) {
}
