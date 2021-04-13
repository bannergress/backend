package com.bannergress.backend.services.impl;

import com.bannergress.backend.dto.*;
import com.bannergress.backend.entities.Mission;
import com.bannergress.backend.entities.MissionStep;
import com.bannergress.backend.entities.NamedAgent;
import com.bannergress.backend.entities.POI;
import com.bannergress.backend.enums.POIType;
import com.bannergress.backend.services.AgentService;
import com.bannergress.backend.services.BannerService;
import com.bannergress.backend.services.MissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.time.Instant;
import java.util.*;

/**
 * Default implementation of {@link MissionService}.
 */
@Service
@Transactional(isolation = Isolation.SERIALIZABLE)
public class MissionServiceImpl implements MissionService {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AgentService agentService;

    @Autowired
    private BannerService bannerService;

    @Override
    public Mission importMission(IntelMissionDetails data) {
        Set<String> missionsWithBannerAffectingChanges = new HashSet<>();
        Set<String> poisWithBannerAffectingChanges = new HashSet<>();
        Mission mission = importMissionSummary(data, missionsWithBannerAffectingChanges);
        NamedAgent author = agentService.importAgent(data.authorName, data.authorFaction);
        if (mission.getLatestUpdateDetails() == null) {
            missionsWithBannerAffectingChanges.add(mission.getId());
        }
        Instant now = Instant.now();
        mission.setLatestUpdateSummary(now);
        mission.setLatestUpdateDetails(now);
        mission.setAuthor(author);
        mission.setDescription(data.description);
        mission.setNumberCompleted(data.numberCompleted);
        mission.setType(data.type);
        List<IntelMissionStep> steps = data.steps;
        for (int i = 0; i < steps.size(); i++) {
            IntelMissionStep intelMissionStep = steps.get(i);
            MissionStep missionStep;
            if (i < mission.getSteps().size()) {
                missionStep = mission.getSteps().get(i);
            } else {
                missionStep = new MissionStep();
                missionStep.setMission(mission);
                mission.getSteps().add(missionStep);
            }
            importMissionStep(intelMissionStep, missionStep, poisWithBannerAffectingChanges);
            entityManager.persist(missionStep);
        }
        for (int i = mission.getSteps().size() - 1; i >= steps.size(); i--) {
            mission.getSteps().remove(i);
        }
        bannerService.updateBannersContainingMission(missionsWithBannerAffectingChanges);
        bannerService.updateBannersContainingPOI(poisWithBannerAffectingChanges);
        return mission;
    }

    private void importMissionStep(IntelMissionStep intelMissionStep, MissionStep missionStep,
                                   Set<String> poisWithBannerAffectingChanges) {
        double newLatitude = intelMissionStep.latitudeE6 / 1_000_000d;
        double newLongitude = intelMissionStep.longitudeE6 / 1_000_000d;
        POI poi = entityManager.find(POI.class, intelMissionStep.id);
        if (poi == null) {
            poi = new POI();
            poi.setId(intelMissionStep.id);
        } else if (!Objects.equals(missionStep.getObjective(), intelMissionStep.objective)
            || !Objects.equals(poi.getType(), intelMissionStep.type) || !Objects.equals(poi.getLatitude(), newLatitude)
            || !Objects.equals(poi.getLongitude(), newLongitude)) {
            poisWithBannerAffectingChanges.add(intelMissionStep.id);
        }
        missionStep.setObjective(intelMissionStep.objective);
        poi.setType(intelMissionStep.type);
        if (intelMissionStep.type != POIType.unavailable) {
            poi.setLatitude(newLatitude);
            poi.setLongitude(newLongitude);
            poi.setTitle(intelMissionStep.title);
            poi.setPicture(intelMissionStep.picture);
        }
        missionStep.setPoi(poi);
        entityManager.persist(poi);
    }

    @Override
    public Collection<Mission> importTopMissionsInBounds(IntelTopMissionsInBounds data) {
        return importMissionSummaries(data.summaries);
    }

    @Override
    public Collection<Mission> importTopMissionsForPortal(IntelTopMissionsForPortal data) {
        return importMissionSummaries(data.summaries);
    }

    private Collection<Mission> importMissionSummaries(List<IntelMissionSummary> summaries) {
        List<Mission> imported = new ArrayList<>();
        Set<String> missionsWithBannerAffectingChanges = new HashSet<>();
        for (IntelMissionSummary summary : summaries) {
            imported.add(importMissionSummary(summary, missionsWithBannerAffectingChanges));
        }
        bannerService.updateBannersContainingMission(missionsWithBannerAffectingChanges);
        return imported;
    }

    private Mission importMissionSummary(IntelMissionSummary data,
                                         Collection<String> missionsWithBannerAffectingChanges) {
        double newRating = data.ratingE6 / 1_000_000.;
        Mission mission = entityManager.find(Mission.class, data.id);
        if (mission == null) {
            mission = new Mission();
            mission.setId(data.id);
        } else if (!Objects.equals(mission.getPicture(), data.picture)
            || !Objects.equals(mission.getAverageDurationMilliseconds(), data.averageDurationMilliseconds)
            || !mission.isOnline()) {
            missionsWithBannerAffectingChanges.add(mission.getId());
        }
        mission.setLatestUpdateSummary(Instant.now());
        mission.setTitle(data.title);
        mission.setPicture(data.picture);
        mission.setAverageDurationMilliseconds(data.averageDurationMilliseconds);
        mission.setRating(newRating);
        mission.setOnline(true);
        entityManager.persist(mission);
        return mission;
    }

    @Override
    public Collection<Mission> findUnusedMissions(String search, int maxResults) {
        TypedQuery<Mission> query = entityManager.createQuery("SELECT m FROM Mission m WHERE m.title LIKE :search "
            + "AND NOT EXISTS (SELECT b FROM Banner b WHERE m MEMBER OF b.missions)", Mission.class);
        query.setParameter("search", "%" + search + "%");
        query.setMaxResults(maxResults);
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
    public void verifyAvailability(Collection<String> ids) {
        TypedQuery<Long> query = entityManager.createQuery("SELECT COUNT(m) FROM Mission m WHERE m.id IN :ids "
            + "AND NOT EXISTS (SELECT b FROM Banner b WHERE m MEMBER OF b.missions)", Long.class);
        query.setParameter("ids", ids);
        long availableMissions = query.getSingleResult();
        if (availableMissions < ids.size()) {
            throw new IllegalArgumentException();
        }
    }
}
