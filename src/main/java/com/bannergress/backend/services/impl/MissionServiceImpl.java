package com.bannergress.backend.services.impl;

import com.bannergress.backend.dto.*;
import com.bannergress.backend.entities.Mission;
import com.bannergress.backend.entities.MissionStep;
import com.bannergress.backend.entities.NamedAgent;
import com.bannergress.backend.entities.POI;
import com.bannergress.backend.enums.POIType;
import com.bannergress.backend.exceptions.MissionAlreadyUsedException;
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
    private static final int INTEL_TOP_MISSIONS_IN_BOUNDS_LIMIT = 25;

    private static final int INTEL_TOP_MISSIONS_FOR_PORTAL_LIMIT = 10;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AgentService agentService;

    @Autowired
    private BannerService bannerService;

    private Optional<String> latestRefreshableMission = Optional.empty();

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
        double newLatitude = fromE6(intelMissionStep.latitudeE6);
        double newLongitude = fromE6(intelMissionStep.longitudeE6);
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
        Collection<Mission> missions = importMissionSummaries(data.summaries);
        if (missions.size() < INTEL_TOP_MISSIONS_IN_BOUNDS_LIMIT) {
            setMissionsOfflineInBounds(fromE6(data.request.southE6), fromE6(data.request.westE6),
                fromE6(data.request.northE6), fromE6(data.request.eastE6), missions);
        }
        return missions;
    }

    @Override
    public Collection<Mission> importTopMissionsForPortal(IntelTopMissionsForPortal data) {
        Collection<Mission> missions = importMissionSummaries(data.summaries);
        if (missions.size() < INTEL_TOP_MISSIONS_FOR_PORTAL_LIMIT) {
            setMissionsOfflineForPortal(data.request.guid, missions);
        }
        return missions;
    }

    private void setMissionsOfflineInBounds(double minLatitude, double minLongitude, double maxLatitude,
                                            double maxLongitude, Collection<Mission> exclude) {
        TypedQuery<Mission> query = entityManager.createQuery("SELECT m FROM Mission m WHERE m NOT IN :exclude"
            + " AND m.steps[0].poi.latitude BETWEEN :minLatitude AND :maxLatitude"
            + " AND m.steps[0].poi.longitude BETWEEN :minLongitude AND :maxLongitude", Mission.class);
        query.setParameter("exclude", exclude);
        query.setParameter("minLatitude", minLatitude);
        query.setParameter("minLongitude", minLongitude);
        query.setParameter("maxLatitude", maxLatitude);
        query.setParameter("maxLongitude", maxLongitude);
        setMissionsOffline(query.getResultList());
    }

    private void setMissionsOfflineForPortal(String startPoiId, Collection<Mission> exclude) {
        TypedQuery<Mission> query = entityManager.createQuery(
            "SELECT m FROM Mission m WHERE m NOT IN :exclude AND m.steps[0].poi.id = :startPoiId", Mission.class);
        query.setParameter("exclude", exclude);
        query.setParameter("startPoiId", startPoiId);
        setMissionsOffline(query.getResultList());
    }

    private void setMissionsOffline(List<Mission> missions) {
        for (Mission mission : missions) {
            mission.setOnline(false);
        }
    }

    public Collection<Mission> importMissionSummaries(List<IntelMissionSummary> summaries) {
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
        double newRating = fromE6(data.ratingE6);
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
    public void assertNotAlreadyUsedInBanners(Collection<String> ids) throws MissionAlreadyUsedException {
        TypedQuery<Long> query = entityManager.createQuery("SELECT COUNT(m) FROM Mission m WHERE m.id IN :ids "
            + "AND NOT EXISTS (SELECT b FROM Banner b WHERE m MEMBER OF b.missions)", Long.class);
        query.setParameter("ids", ids);
        long availableMissions = query.getSingleResult();
        if (availableMissions < ids.size()) {
            throw new MissionAlreadyUsedException();
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

    private static double fromE6(int e6) {
        return e6 / 1_000_000d;
    }
}
