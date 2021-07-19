package com.bannergress.backend.repositories;

import com.bannergress.backend.entities.Banner;
import com.bannergress.backend.entities.BannerSettings;
import com.bannergress.backend.entities.Mission;
import com.bannergress.backend.entities.Place;
import com.bannergress.backend.enums.BannerListType;
import com.google.common.base.Preconditions;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;

import java.util.Collection;
import java.util.EnumSet;

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

    public static Specification<Banner> isInUserList(Collection<BannerListType> listTypes, String userId) {
        EnumSet<BannerListType> otherListTypes = EnumSet.complementOf(EnumSet.copyOf(listTypes));
        if (otherListTypes.isEmpty()) {
            // All possible list types, so no filtering needed
            return null;
        }
        if (listTypes.contains(BannerListType.none)) {
            // Banners without settings default to BannerListType.none.
            // Therefore, we need to check that no user settings with the remaining types exist.
            return Specification.not(isInUserList(otherListTypes, userId));
        }
        return (banner, cq, cb) -> {
            Subquery<BannerSettings> subquery = cq.subquery(BannerSettings.class);
            Root<BannerSettings> settings = subquery.from(BannerSettings.class);
            subquery.select(settings).where(cb.equal(settings.get("banner"), banner),
                cb.equal(settings.get("user").get("id"), userId), settings.get("listType").in(listTypes));
            return cb.exists(subquery);
        };
    }

    public static Specification<Banner> isInUserListSorted(Collection<BannerListType> listTypes, String userId,
                                                           Direction direction) {
        Preconditions.checkArgument(!listTypes.contains(BannerListType.none));
        return (banner, cq, cb) -> {
            Join<Banner, BannerSettings> settings = banner.join("settings");
            switch (direction) {
                case ASC:
                    cq.orderBy(cb.asc(settings.get("listAdded")), cb.asc(banner.get("uuid")));
                    break;
                case DESC:
                    cq.orderBy(cb.desc(settings.get("listAdded")), cb.desc(banner.get("uuid")));
                    break;
            }
            return cb.and(cb.equal(settings.get("user").get("id"), userId), settings.get("listType").in(listTypes));
        };
    }
}
