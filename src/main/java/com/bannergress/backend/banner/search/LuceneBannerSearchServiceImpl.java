package com.bannergress.backend.banner.search;

import com.bannergress.backend.banner.Banner;
import com.bannergress.backend.banner.BannerListType;
import com.bannergress.backend.banner.settings.BannerSettings;
import com.bannergress.backend.banner.settings.BannerSettingsRepository;
import com.google.common.base.Preconditions;
import com.google.common.collect.Ordering;
import jakarta.persistence.EntityManager;
import org.hibernate.search.engine.search.common.BooleanOperator;
import org.hibernate.search.engine.search.predicate.dsl.SimpleBooleanPredicateClausesStep;
import org.hibernate.search.engine.search.predicate.dsl.SimpleQueryStringPredicateFieldMoreStep;
import org.hibernate.search.engine.search.sort.dsl.SortOrder;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * Lucene-based implementation of {@link BannerSearchService}.
 */
@Service
@Transactional
class LuceneBannerSearchServiceImpl extends BaseBannerSearchServiceImpl {
    private static final String FIELD_UUID = "uuid";
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_TITLE_SORT = "titleSort";
    private static final String FIELD_DESCRIPTION = "description";
    private static final String FIELD_CREATED = "created";
    private static final String FIELD_LENGTH_METERS = "lengthMeters";
    private static final String FIELD_NUMBER_OF_MISSIONS = "numberOfMissions";
    private static final String FIELD_ONLINE = "online";
    private static final String FIELD_START_POINT = "startPoint";
    private static final String FIELD_START_PLACES_SLUG = "startPlaces.slug";
    private static final String FIELD_START_PLACES_INFORMATION_FORMATTED_ADDRESS = "startPlaces.information.formattedAddress";
    private static final String FIELD_START_PLACES_INFORMATION_LONG_NAME = "startPlaces.information.longName";
    private static final String FIELD_MISSIONS_AUTHOR_NAME = "missions.author.name";
    private static final String FIELD_MISSIONS_TITLE = "missions.title";
    private static final String FIELD_MISSIONS_ID = "missions.id";
    private static final String FIELD_EVENT_START_TIMESTAMP = "eventStartTimestamp";
    private static final String FIELD_EVENT_END_TIMESTAMP = "eventEndTimestamp";

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private BannerSettingsRepository bannerSettingsRepository;

    @Override
    public List<Banner> find(Optional<String> placeSlug, Optional<Double> minLatitude, Optional<Double> maxLatitude,
                             Optional<Double> minLongitude, Optional<Double> maxLongitude, Optional<String> search,
                             boolean queryAuthor, Optional<String> missionId, boolean onlyOfficialMissions,
                             Optional<String> author, Optional<Collection<BannerListType>> listTypes,
                             Optional<String> userId, Optional<Boolean> online, Optional<BannerSortOrder> orderBy,
                             Direction orderDirection, Optional<Double> proximityLatitude,
                             Optional<Double> proximityLongitude, Optional<Instant> minEventTimestamp,
                             Optional<Instant> maxEventTimestamp, int offset, int limit) {
        boolean sortAndPaginateInHibernateSearch = orderBy.map(x -> x != BannerSortOrder.listAdded).orElse(true);
        ListTypeFilter listTypeFilter = listTypes.map(x -> getListTypeFilter(x, userId.get()))
            .orElseGet(() -> new ListTypeFilter(null, ListTypeFilter.Method.IGNORE));
        SearchSession searchSession = Search.session(entityManager);
        List<Banner> result = searchSession.search(Banner.class).where((factory, predicate) -> {
            predicate.add(factory.matchAll());
            if (placeSlug.isPresent()) {
                predicate.add(factory.match().field(FIELD_START_PLACES_SLUG).matching(placeSlug.get()));
            }
            if (minLatitude.isPresent()) {
                predicate.add(factory.spatial().within().field(FIELD_START_POINT).boundingBox(maxLatitude.get(),
                    minLongitude.get(), minLatitude.get(), maxLongitude.get()));
            }
            if (search.isPresent()) {
                SimpleQueryStringPredicateFieldMoreStep<?, ?, ?> step = factory.simpleQueryString() //
                    .field(FIELD_TITLE).boost(5) //
                    .field(FIELD_DESCRIPTION).boost(0.1f) //
                    .field(FIELD_MISSIONS_ID) //
                    .field(FIELD_MISSIONS_TITLE) //
                    .field(FIELD_START_PLACES_INFORMATION_LONG_NAME) //
                    .field(FIELD_START_PLACES_INFORMATION_FORMATTED_ADDRESS);
                if (queryAuthor) {
                    step = step.field(FIELD_MISSIONS_AUTHOR_NAME);
                }
                predicate.add(step.matching(search.get()).defaultOperator(BooleanOperator.AND));
            }
            if (missionId.isPresent()) {
                predicate.add(factory.match().field(FIELD_MISSIONS_ID).matching(missionId.get()));
            }
            if (onlyOfficialMissions) {
                SimpleBooleanPredicateClausesStep<?, ?> orPredicate = factory.or();
                for (String officialMissionAuthor : nianticConfiguration.officialMissionAuthors()) {
                    orPredicate.add(factory.match().field(FIELD_MISSIONS_AUTHOR_NAME).matching(officialMissionAuthor));
                }
                predicate.add(orPredicate);
            }
            if (author.isPresent()) {
                predicate.add(factory.match().field(FIELD_MISSIONS_AUTHOR_NAME).matching(author.get()));
            }
            switch (listTypeFilter.method()) {
                case INCLUDE -> predicate.add(factory.id().matchingAny(listTypeFilter.uuidsSorted()));
                case EXCLUDE -> predicate.add(factory.not(factory.id().matchingAny(listTypeFilter.uuidsSorted())));
                case IGNORE -> {
                }
            }
            if (online.isPresent()) {
                predicate.add(factory.match().field(FIELD_ONLINE).matching(online.get()));
            }
            if (minEventTimestamp.isPresent()) {
                predicate.add(factory.range().field(FIELD_EVENT_END_TIMESTAMP).greaterThan(minEventTimestamp.get()));
            }
            if (maxEventTimestamp.isPresent()) {
                predicate.add(factory.range().field(FIELD_EVENT_START_TIMESTAMP).atMost(maxEventTimestamp.get()));
            }
        }).sort(factory -> {
            if (sortAndPaginateInHibernateSearch) {
                return factory.composite(b -> {
                    if (orderBy.isPresent()) {
                        SortOrder direction = orderDirection == Direction.ASC ? SortOrder.ASC : SortOrder.DESC;
                        switch (orderBy.get()) {
                            case created:
                                b.add(factory.field(FIELD_CREATED).order(direction));
                                break;
                            case lengthMeters:
                                b.add(factory.field(FIELD_LENGTH_METERS).order(direction));
                                break;
                            case numberOfMissions:
                                b.add(factory.field(FIELD_NUMBER_OF_MISSIONS).order(direction));
                                break;
                            case proximityStartPoint:
                                b.add(factory
                                    .distance(FIELD_START_POINT, proximityLatitude.get(), proximityLongitude.get())
                                    .order(direction));
                                break;
                            case relevance:
                                b.add(factory.score().order(direction));
                                break;
                            case title:
                                b.add(factory.field(FIELD_TITLE_SORT).order(direction));
                                break;
                            default:
                                throw new IllegalArgumentException(orderBy.get().toString());
                        }
                    }
                    b.add(factory.field(FIELD_UUID));
                });
            } else {
                return factory.indexOrder();
            }
        }).fetchHits(sortAndPaginateInHibernateSearch ? offset : null, sortAndPaginateInHibernateSearch ? limit : null);
        if (!sortAndPaginateInHibernateSearch) {
            result = result.stream() //
                .sorted(createListTypeFilterComparator(listTypeFilter, orderDirection)) //
                .skip(offset) //
                .limit(limit) //
                .toList();
        }
        preloadPlaceInformation(result);
        return result;
    }

    private Comparator<Banner> createListTypeFilterComparator(ListTypeFilter filter, Direction orderDirection) {
        Preconditions.checkArgument(filter.method() == ListTypeFilter.Method.INCLUDE);
        Comparator<Banner> comparator = Comparator.comparing(Banner::getUuid, Ordering.explicit(filter.uuidsSorted()));
        return switch (orderDirection) {
            case ASC -> comparator;
            case DESC -> comparator.reversed();
        };
    }

    private ListTypeFilter getListTypeFilter(Collection<BannerListType> listTypes, String userId) {
        EnumSet<BannerListType> otherListTypes = EnumSet.complementOf(EnumSet.copyOf(listTypes));
        if (otherListTypes.isEmpty()) {
            // All possible list types, so no filtering needed
            return new ListTypeFilter(null, ListTypeFilter.Method.IGNORE);
        } else if (listTypes.contains(BannerListType.none)) {
            // Banners without settings default to BannerListType.none.
            // Therefore, we need to check that no user settings with the remaining types exist.
            ListTypeFilter inverseFilter = getListTypeFilter(otherListTypes, userId);
            return new ListTypeFilter(inverseFilter.uuidsSorted(), ListTypeFilter.Method.EXCLUDE);
        } else {
            List<BannerSettings> settings = bannerSettingsRepository.findAllByUserIdAndListTypeIn(userId, listTypes);
            List<UUID> uuids = settings.stream() //
                .sorted(Comparator.comparing(BannerSettings::getListAdded).thenComparing(BannerSettings::getUuid)) //
                .map(x -> x.getBanner().getUuid()) //
                .toList();
            return new ListTypeFilter(uuids, ListTypeFilter.Method.INCLUDE);
        }
    }

    @Override
    public void updateIndex() {
        try {
            Search.session(entityManager).massIndexer(Banner.class).startAndWait();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
