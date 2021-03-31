package com.bannergress.backend.services.impl;

import com.bannergress.backend.dto.IntelMissionDetails;
import com.bannergress.backend.dto.IntelMissionStep;
import com.bannergress.backend.dto.IntelMissionSummary;
import com.bannergress.backend.dto.IntelTopMissionsInBounds;
import com.bannergress.backend.entities.Mission;
import com.bannergress.backend.entities.MissionStep;
import com.bannergress.backend.entities.NamedAgent;
import com.bannergress.backend.entities.POI;
import com.bannergress.backend.enums.POIType;
import com.bannergress.backend.services.AgentService;
import com.bannergress.backend.services.MissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

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

    @Override
    public void importMission(IntelMissionDetails data) {
        Mission mission = importMissionSummary(data);
        NamedAgent author = agentService.importAgent(data.authorName, data.authorFaction);
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
            importMissionStep(intelMissionStep, missionStep);
            entityManager.persist(missionStep);
        }
        for (int i = mission.getSteps().size() - 1; i >= steps.size(); i--) {
            mission.getSteps().remove(i);
        }
    }

    private void importMissionStep(IntelMissionStep intelMissionStep, MissionStep missionStep) {
        missionStep.setObjective(intelMissionStep.objective);
        POI poi = entityManager.find(POI.class, intelMissionStep.id);
        if (poi == null) {
            poi = new POI();
            poi.setId(intelMissionStep.id);
        }
        poi.setType(intelMissionStep.type);
        if (intelMissionStep.type != POIType.unavailable) {
            poi.setLatitude(intelMissionStep.latitudeE6 / 1_000_000d);
            poi.setLongitude(intelMissionStep.longitudeE6 / 1_000_000d);
            poi.setTitle(intelMissionStep.title);
            poi.setPicture(intelMissionStep.picture);
        }
        missionStep.setPoi(poi);
        entityManager.persist(poi);
    }

    @Override
    public void importTopMissionsInBounds(IntelTopMissionsInBounds data) {
        List<Mission> imported = new ArrayList<>();
        for (IntelMissionSummary summary : data.summaries) {
            imported.add(importMissionSummary(summary));
        }
    }

    private Mission importMissionSummary(IntelMissionSummary data) {
        Mission mission = entityManager.find(Mission.class, data.id);
        if (mission == null) {
            mission = new Mission();
            mission.setId(data.id);
        }
        mission.setTitle(data.title);
        mission.setPicture(data.picture);
        mission.setAverageDurationMilliseconds(data.averageDurationMilliseconds);
        mission.setRating(data.ratingE6 / 1_000_000.);
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
