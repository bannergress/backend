package com.bannergress.backend.services.impl;

import com.bannergress.backend.entities.Banner;
import com.bannergress.backend.entities.Mission;
import com.bannergress.backend.enums.MissionSortOrder;
import com.bannergress.backend.exceptions.MissionAlreadyUsedException;
import com.bannergress.backend.repositories.MissionRepository;
import com.bannergress.backend.services.MissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
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
        String queryString = "SELECT m FROM Mission m WHERE (LOWER(m.title) LIKE :search "
            + "OR LOWER(m.author.name) = :searchExact)"
            + "AND m.banners IS EMPTY AND m.latestUpdateDetails IS NOT NULL";
        if (orderBy.isPresent()) {
            switch (orderBy.get()) {
                case title:
                    queryString += " ORDER BY m.title " + orderDirection.toString();
                    break;
            }
        }
        TypedQuery<Mission> query = entityManager.createQuery(queryString, Mission.class);
        query.setParameter("search", "%" + search.toLowerCase() + "%");
        query.setParameter("searchExact", search.toLowerCase());
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    @Override
    public Optional<Mission> findById(String id) {
        return Optional.ofNullable(entityManager.find(Mission.class, id));
    }

    @Override
    public Collection<Mission> findByIds(Collection<String> ids) {
        TypedQuery<Mission> query = entityManager.createQuery("SELECT m FROM Mission m WHERE m.id IN :ids",
            Mission.class);
        query.setParameter("ids", ids);
        return query.getResultList();
    }

    @Override
    public void assertNotAlreadyUsedInBanners(Collection<String> ids, List<String> acceptableBannerSlugs)
        throws MissionAlreadyUsedException {
        for (String missionId : ids) {
            Mission mission = missionRepository.getOne(missionId);
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
