package com.bannergress.backend.repositories;

import com.bannergress.backend.entities.Banner;
import com.bannergress.backend.entities.Mission;
import com.bannergress.backend.entities.Place;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;

import java.util.Collection;

/**
 * Specifications for banner searches.
 */
public class BannerSpecifications {
    public static Specification<Banner> fetchDetails() {
        return (banner, cq, cb) -> {
            Fetch<Banner, Mission> missions = banner.fetch("missions", JoinType.LEFT);
            missions.fetch("author", JoinType.LEFT);
            missions.fetch("steps", JoinType.LEFT).fetch("poi", JoinType.LEFT);
            return null;
        };
    }

    public static Specification<Banner> fetchPlaceInformation() {
        return (banner, cq, cb) -> {
            banner.fetch("startPlaces", JoinType.LEFT).fetch("information", JoinType.LEFT);
            return null;
        };
    }

    public static Specification<Banner> hasMissionWith(Specification<Mission> missionSpecification) {
        return (banner, cq, cb) -> {
            Subquery<Mission> subquery = cq.subquery(Mission.class);
            Root<Mission> mission = subquery.from(Mission.class);
            Join<Mission, Banner> join = mission.join("banners");
            subquery.select(mission);
            Predicate bannerPredicate = cb.equal(join.get("uuid"), banner.get("uuid"));
            Predicate missionPredicate = missionSpecification.toPredicate(mission, cq, cb);
            subquery.select(mission).where(bannerPredicate, missionPredicate);
            return cb.exists(subquery);
        };
    }

    public static Specification<Banner> hasMissionId(String missionId) {
        return (banner, cq, cb) -> {
            Join<Banner, Mission> mission = banner.join("missions");
            return cb.equal(mission.get("id"), missionId);
        };
    }

    public static Specification<Banner> hasSlug(String slug) {
        return (banner, cq, cb) -> cb.equal(banner.get("slug"), slug);
    }

    public static Specification<Banner> hasStartPlaceSlug(String startPlaceSlug) {
        return (banner, cq, cb) -> {
            Join<Banner, Place> place = banner.join("startPlaces");
            return cb.equal(place.get("slug"), startPlaceSlug);
        };
    }

    public static Specification<Banner> hasTitlePart(String titlePart) {
        return (banner, cq, cb) -> cb.like(cb.lower(banner.get("title")), "%" + titlePart.toLowerCase() + "%");
    }

    public static Specification<Banner> isInBanners(Collection<Banner> banners) {
        return (banner, cq, cb) -> banner.in(banners);
    }

    public static Specification<Banner> isInLatitudeRange(double minLatitude, double maxLatitude) {
        return (banner, cq, cb) -> cb.between(banner.get("startLatitude"), minLatitude, maxLatitude);
    }

    public static Specification<Banner> isInLongitudeRange(double minLongitude, double maxLongitude) {
        return (banner, cq, cb) -> cb.between(banner.get("startLongitude"), minLongitude, maxLongitude);
    }
}
