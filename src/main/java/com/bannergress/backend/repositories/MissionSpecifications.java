package com.bannergress.backend.repositories;

import com.bannergress.backend.entities.Mission;
import com.bannergress.backend.entities.MissionStep_;
import com.bannergress.backend.entities.Mission_;
import com.bannergress.backend.entities.NamedAgent_;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.JoinType;

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
            return cb.lower(mission.get(Mission_.author).get(NamedAgent_.name)).in(lowercaseAuthors);
        };
    }

    public static Specification<Mission> isInMissions(Collection<Mission> missions) {
        return (mission, cq, cb) -> mission.in(missions);
    }

    public static Specification<Mission> fetchDetails() {
        return (mission, cq, cb) -> {
            mission.fetch(Mission_.author, JoinType.LEFT);
            mission.fetch(Mission_.steps, JoinType.LEFT).fetch(MissionStep_.poi, JoinType.LEFT);
            return null;
        };
    }
}
