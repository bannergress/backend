package com.bannergress.backend.banner.search;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

/**
 * Niantic-related configuration.
 *
 * @param officialMissionAuthors Ingress accounts that are used by Niantic.
 */
@ConfigurationProperties("niantic")
public record NianticConfiguration(Set<String> officialMissionAuthors) {
}
