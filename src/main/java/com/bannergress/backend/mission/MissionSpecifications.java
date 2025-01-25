package com.bannergress.backend.mission;

import com.bannergress.backend.agent.NamedAgent_;
import com.bannergress.backend.mission.step.MissionStep_;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

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

    public static Specification<Mission> hasNoBanners() {
        return (mission, cq, cb) -> cb.isEmpty(mission.get(Mission_.banners));
    }

    public static Specification<Mission> hasTitlePart(String titlePart) {
        return (mission, cq, cb) -> cb.like(cb.lower(mission.get(Mission_.title)), "%" + titlePart.toLowerCase() + "%");
    }

    public static Specification<Mission> hasLatestUpdateDetails() {
        return (mission, cq, cb) -> mission.get(Mission_.latestUpdateDetails).isNotNull();
    }

    public static Specification<Mission> hasIds(Collection<String> ids) {
        return (mission, cq, cb) -> mission.get(Mission_.id).in(ids);
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
