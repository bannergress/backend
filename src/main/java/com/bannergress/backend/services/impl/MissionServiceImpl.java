package com.bannergress.backend.services.impl;

import com.bannergress.backend.entities.Banner;
import com.bannergress.backend.entities.Mission;
import com.bannergress.backend.entities.Mission_;
import com.bannergress.backend.enums.MissionSortOrder;
import com.bannergress.backend.exceptions.MissionAlreadyUsedException;
import com.bannergress.backend.repositories.MissionRepository;
import com.bannergress.backend.repositories.MissionSpecifications;
import com.bannergress.backend.services.MissionService;
import com.bannergress.backend.utils.OffsetBasedPageRequest;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import java.util.*;

/**
 * Default implementation of {@link MissionService}.
 */
@Service
@Transactional
public class MissionServiceImpl implements MissionService {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private MissionRepository missionRepository;

    private Optional<String> latestRefreshableMission = Optional.empty();

    @Override
    public Collection<Mission> findUnusedMissions(String search, Optional<MissionSortOrder> orderBy,
                                                  Direction orderDirection, int offset, int limit) {
        Specification<Mission> specification = (MissionSpecifications.hasTitlePart(search)
            .or(MissionSpecifications.hasAuthors(ImmutableList.of(search)))) //
                .and(MissionSpecifications.hasNoBanners()) //
                .and(MissionSpecifications.hasLatestUpdateDetails());
        Sort sort = orderBy.map(order -> {
            switch (order) {
                case title:
                    return Sort.by(orderDirection, Mission_.TITLE);
                default:
                    return Sort.by(orderDirection, Mission_.ID);
            }
        }).orElse(Sort.by(Direction.ASC, Mission_.ID));
        OffsetBasedPageRequest request = new OffsetBasedPageRequest(offset, limit, sort);
        return missionRepository.findAll(specification, request).getContent();
    }

    @Override
    public Optional<Mission> findById(String id) {
        return missionRepository.findById(id);
    }

    @Override
    public Collection<Mission> findByIds(Collection<String> ids) {
        return missionRepository.findAll(MissionSpecifications.hasIds(ids));
    }

    @Override
    public void assertNotAlreadyUsedInBanners(Collection<String> ids, List<String> acceptableBannerSlugs)
        throws MissionAlreadyUsedException {
        for (String missionId : ids) {
            Mission mission = missionRepository.getById(missionId);
            for (Banner banner : mission.getBanners()) {
                if (!acceptableBannerSlugs.contains(banner.getCanonicalSlug())) {
                    throw new MissionAlreadyUsedException();
                }
            }
        }
    }

    @Override
    public synchronized Collection<String> findNextRequestedMissions(int amount) {
        final List<String> missionIds = new ArrayList<>();
        if (latestRefreshableMission.isPresent()) {
            TypedQuery<String> query = entityManager.createQuery("SELECT m.id FROM Mission m"
                + " WHERE m.id > :latestId AND m.latestUpdateDetails IS NULL ORDER BY m.id", String.class);
            query.setMaxResults(amount);
            query.setParameter("latestId", latestRefreshableMission.get());
            missionIds.addAll(query.getResultList());
        }
        if (missionIds.size() < amount) {
            TypedQuery<String> query = entityManager.createQuery(
                "SELECT m.id FROM Mission m WHERE m.latestUpdateDetails IS NULL ORDER BY m.id", String.class);
            query.setMaxResults(amount - missionIds.size());
            missionIds.addAll(query.getResultList());
        }
        final Set<String> result = new HashSet<>(missionIds);
        latestRefreshableMission = result.size() < amount ? Optional.empty() : Optional.of(missionIds.get(amount - 1));
        return result;
    }
}
