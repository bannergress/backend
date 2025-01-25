package com.bannergress.backend.mission;

import com.bannergress.backend.banner.Banner;
import com.bannergress.backend.banner.MissionAlreadyUsedException;
import com.bannergress.backend.utils.OffsetBasedPageRequest;
import com.google.common.collect.ImmutableList;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Default implementation of {@link MissionService}.
 */
@Service
@Transactional
public class MissionServiceImpl implements MissionService {
    @Autowired
    private MissionRepository missionRepository;

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
            Mission mission = missionRepository.getReferenceById(missionId);
            for (Banner banner : mission.getBanners()) {
                if (!acceptableBannerSlugs.contains(banner.getCanonicalSlug())) {
                    throw new MissionAlreadyUsedException();
                }
            }
        }
    }
}
