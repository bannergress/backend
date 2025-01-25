package com.bannergress.backend.banner.search;

import com.bannergress.backend.banner.Banner;
import com.bannergress.backend.banner.BannerListType;
import com.bannergress.backend.banner.BannerSpecifications;
import com.bannergress.backend.mission.MissionSpecifications;
import com.bannergress.backend.spatial.Spatial;
import com.bannergress.backend.utils.OffsetBasedPageRequest;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Database-based implementation of {@link BannerSearchService}.
 */
@Service
@Profile("databasesearch")
@Transactional
public class DatabaseBannerSearchServiceImpl extends BaseBannerSearchServiceImpl {
    @Autowired
    private Spatial spatial;

    @Override
    public List<Banner> find(Optional<String> placeSlug, Optional<Double> minLatitude, Optional<Double> maxLatitude,
                             Optional<Double> minLongitude, Optional<Double> maxLongitude, Optional<String> search,
                             boolean queryAuthor, Optional<String> missionId, boolean onlyOfficialMissions,
                             Optional<String> author, Optional<Collection<BannerListType>> listTypes,
                             Optional<String> userId, Optional<Boolean> online, Optional<BannerSortOrder> orderBy,
                             Direction orderDirection, Optional<Double> proximityLatitude,
                             Optional<Double> proximityLongitude, Optional<Instant> minEventTimestamp,
                             Optional<Instant> maxEventTimestamp, int offset, int limit) {
        List<Specification<Banner>> specifications = new ArrayList<>();
        if (placeSlug.isPresent()) {
            specifications.add(BannerSpecifications.hasStartPlaceSlug(placeSlug.get()));
        }
        if (minLatitude.isPresent()) {
            if (minLongitude.get() <= maxLongitude.get()) {
                Geometry box = spatial.createBoundingBox(minLatitude.get(), maxLatitude.get(), minLongitude.get(),
                    maxLongitude.get());
                specifications.add(BannerSpecifications.startPointIntersects(box));
            } else {
                Geometry box1 = spatial.createBoundingBox(minLatitude.get(), maxLatitude.get(), maxLongitude.get(),
                    180);
                Geometry box2 = spatial.createBoundingBox(minLatitude.get(), maxLatitude.get(), -180,
                    minLongitude.get());
                specifications.add(BannerSpecifications.startPointIntersects(box1)
                    .or(BannerSpecifications.startPointIntersects(box2)));
            }
        }
        if (search.isPresent()) {
            specifications.add(BannerSpecifications.hasTitlePart(search.get()));
        }
        if (missionId.isPresent()) {
            specifications.add(BannerSpecifications.hasMissionId(missionId.get()));
        }
        if (onlyOfficialMissions) {
            specifications
                .add(BannerSpecifications.hasMissionWith(MissionSpecifications.hasAuthors(OFFICIAL_MISSION_AUTHORS)));
        }
        if (author.isPresent()) {
            specifications
                .add(BannerSpecifications.hasMissionWith(MissionSpecifications.hasAuthors(List.of(author.get()))));
        }
        if (listTypes.isPresent()) {
            if (orderBy.isPresent() && orderBy.get() == BannerSortOrder.listAdded) {
                specifications
                    .add(BannerSpecifications.isInUserListSorted(listTypes.get(), userId.get(), orderDirection));
            } else {
                specifications.add(BannerSpecifications.isInUserList(listTypes.get(), userId.get()));
            }
        }
        if (online.isPresent()) {
            specifications.add(BannerSpecifications.hasOnline(online.get()));
        }
        if (minEventTimestamp.isPresent()) {
            specifications.add(BannerSpecifications.eventEndsAfter(minEventTimestamp.get()));
        }
        if (maxEventTimestamp.isPresent()) {
            specifications.add(BannerSpecifications.eventStartsBeforeOrAt(minEventTimestamp.get()));
        }

        Sort sort;
        if (orderBy.isPresent()) {
            switch (orderBy.get()) {
                case listAdded:
                    sort = Sort.unsorted(); // Sorting takes place in the specification
                    break;
                case proximityStartPoint:
                    specifications.add(BannerSpecifications.sortByProximity(
                        spatial.createPoint(proximityLatitude.get(), proximityLongitude.get()), orderDirection));
                    sort = Sort.unsorted(); // Sorting takes place in the specification
                    break;
                case relevance:
                    sort = Sort.by(Direction.ASC, "uuid"); // Searching in database does not use relevance
                    break;
                default:
                    sort = Sort.by(orderDirection, orderBy.get().toString(), "uuid");
                    break;
            }
        } else {
            sort = Sort.by(Direction.ASC, "uuid");
        }

        Specification<Banner> fullSpecification = specifications.stream().reduce((a, b) -> a.and(b)).orElse(null);

        OffsetBasedPageRequest request = new OffsetBasedPageRequest(offset, limit, sort);
        List<Banner> banners = bannerRepository.findAll(fullSpecification, request).getContent();
        preloadPlaceInformation(banners);
        return banners;
    }

    @Override
    public void updateIndex() {
        // Nothing to do, database index is always up-to-date
    }
}
