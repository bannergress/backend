package com.bannergress.backend.repositories;

import com.bannergress.backend.entities.Mission;
import com.bannergress.backend.entities.MissionStep_;
import com.bannergress.backend.entities.Mission_;
import com.bannergress.backend.entities.NamedAgent_;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.JoinType;

import java.util.Collection;

/**
 * Specifications for mission searches.
 */
public class MissionSpecifications {
    public static Specification<Mission> hasAuthors(Collection<String> authors) {
        return (mission, cq, cb) -> {
            return mission.get(Mission_.author).get(NamedAgent_.name).in(authors);
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
