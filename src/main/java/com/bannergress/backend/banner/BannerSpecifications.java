package com.bannergress.backend.banner;

import com.bannergress.backend.banner.settings.BannerSettings;
import com.bannergress.backend.banner.settings.BannerSettings_;
import com.bannergress.backend.mission.Mission;
import com.bannergress.backend.mission.Mission_;
import com.bannergress.backend.mission.step.MissionStep_;
import com.bannergress.backend.place.Place;
import com.bannergress.backend.place.Place_;
import com.bannergress.backend.user.User_;
import com.google.common.base.Preconditions;
import jakarta.persistence.criteria.*;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.spatial.criteria.JTSSpatialCriteriaBuilder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.Collection;
import java.util.EnumSet;

/**
 * Specifications for banner searches.
 */
public class BannerSpecifications {
    public static Specification<Banner> fetchDetails() {
        return (banner, cq, cb) -> {
            Fetch<Banner, Mission> missions = banner.fetch(Banner_.missions, JoinType.LEFT);
            missions.fetch(Mission_.author, JoinType.LEFT);
            missions.fetch(Mission_.steps, JoinType.LEFT).fetch(MissionStep_.poi, JoinType.LEFT);
            return null;
        };
    }

    public static Specification<Banner> fetchPlaceInformation() {
        return (banner, cq, cb) -> {
            banner.fetch(Banner_.startPlaces, JoinType.LEFT).fetch(Place_.information, JoinType.LEFT);
            return null;
        };
    }

    public static Specification<Banner> hasMissionWith(Specification<Mission> missionSpecification) {
        return (banner, cq, cb) -> {
            Subquery<Mission> subquery = cq.subquery(Mission.class);
            Root<Mission> mission = subquery.from(Mission.class);
            Join<Mission, Banner> join = mission.join(Mission_.banners);
            subquery.select(mission);
            Predicate bannerPredicate = cb.equal(join.get(Banner_.uuid), banner.get(Banner_.uuid));
            Predicate missionPredicate = missionSpecification.toPredicate(mission, cq, cb);
            subquery.select(mission).where(bannerPredicate, missionPredicate);
            return cb.exists(subquery);
        };
    }

    public static Specification<Banner> hasMissionId(String missionId) {
        return (banner, cq, cb) -> {
            Join<Banner, Mission> mission = banner.join(Banner_.missions);
            return cb.equal(mission.get(Mission_.id), missionId);
        };
    }

    public static Specification<Banner> hasSlug(String slug) {
        return (banner, cq, cb) -> {
            Join<Banner, String> slugs = banner.join(Banner_.slugs);
            return cb.equal(slugs, slug);
        };
    }

    public static Specification<Banner> hasStartPlaceSlug(String startPlaceSlug) {
        return (banner, cq, cb) -> {
            Join<Banner, Place> place = banner.join(Banner_.startPlaces);
            return cb.equal(place.get(Place_.slug), startPlaceSlug);
        };
    }

    public static Specification<Banner> hasTitlePart(String titlePart) {
        return (banner, cq, cb) -> cb.like(cb.lower(banner.get(Banner_.title)), "%" + titlePart.toLowerCase() + "%");
    }

    public static Specification<Banner> hasOnline(boolean online) {
        return (banner, cq, cb) -> cb.equal(banner.get(Banner_.online), online);
    }

    public static Specification<Banner> isInBanners(Collection<Banner> banners) {
        return (banner, cq, cb) -> banner.in(banners);
    }

    public static Specification<Banner> startPointIntersects(Geometry geometry) {
        return (banner, cq, cb) -> ((HibernateCriteriaBuilder) cb).unwrap(JTSSpatialCriteriaBuilder.class)
            .intersects(banner.get(Banner_.startPoint), geometry);
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
            subquery.select(settings).where(cb.equal(settings.get(BannerSettings_.banner), banner),
                cb.equal(settings.get(BannerSettings_.user).get(User_.id), userId),
                settings.get(BannerSettings_.listType).in(listTypes));
            return cb.exists(subquery);
        };
    }

    public static Specification<Banner> isInUserListSorted(Collection<BannerListType> listTypes, String userId,
                                                           Direction direction) {
        Preconditions.checkArgument(!listTypes.contains(BannerListType.none));
        return (banner, cq, cb) -> {
            Join<Banner, BannerSettings> settings = banner.join(Banner_.settings);
            switch (direction) {
                case ASC:
                    cq.orderBy(cb.asc(settings.get(BannerSettings_.listAdded)), cb.asc(banner.get(Banner_.uuid)));
                    break;
                case DESC:
                    cq.orderBy(cb.desc(settings.get(BannerSettings_.listAdded)), cb.desc(banner.get(Banner_.uuid)));
                    break;
            }
            return cb.and(cb.equal(settings.get(BannerSettings_.user).get(User_.id), userId),
                settings.get(BannerSettings_.listType).in(listTypes));
        };
    }

    public static Specification<Banner> eventEndsAfter(Instant instant) {
        return (banner, cq, cb) -> cb.greaterThan(banner.get(Banner_.eventEndTimestamp), instant);
    }

    public static Specification<Banner> eventStartsBeforeOrAt(Instant instant) {
        return (banner, cq, cb) -> cb.lessThanOrEqualTo(banner.get(Banner_.eventStartTimestamp), instant);
    }

    public static Specification<Banner> sortByProximity(Point point, Direction direction) {
        return (banner, cq, cb) -> {
            Expression<Double> sortExpression = distanceSphere(cb, banner.get(Banner_.startPoint), cb.literal(point));
            switch (direction) {
                case ASC:
                    cq.orderBy(cb.asc(sortExpression), cb.asc(banner.get(Banner_.uuid)));
                    break;
                case DESC:
                    cq.orderBy(cb.desc(sortExpression), cb.desc(banner.get(Banner_.uuid)));
                    break;
            }
            return null;
        };
    }

    private static Expression<Double> distanceSphere(CriteriaBuilder cb, Expression<? extends Geometry> geometry1,
                                                     Expression<? extends Geometry> geometry2) {
        return cb.function("ST_DistanceSphere", Double.class, geometry1, geometry2);
    }
}
