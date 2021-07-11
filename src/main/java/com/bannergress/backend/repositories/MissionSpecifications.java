package com.bannergress.backend.repositories;

import com.bannergress.backend.entities.Mission;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Specifications for mission searches.
 */
public class MissionSpecifications {
    public static Specification<Mission> hasAuthors(Collection<String> authors) {
        return (mission, cq, cb) -> {
            List<String> lowercaseAuthors = authors.stream().map(String::toLowerCase).collect(Collectors.toList());
            return cb.lower(mission.get("author").get("name")).in(lowercaseAuthors);
        };
    }
}
