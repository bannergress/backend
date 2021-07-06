package com.bannergress.backend.services.impl;

import com.bannergress.backend.dto.*;
import com.bannergress.backend.entities.Mission;
import com.bannergress.backend.entities.MissionStep;
import com.bannergress.backend.entities.POI;
import com.bannergress.backend.enums.POIType;
import com.bannergress.backend.services.IntelImportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Service for imports from Intel.
 */
@Service
@Transactional
public class IntelImportServiceImpl extends BaseImportServiceImpl implements IntelImportService {
    private static final int INTEL_TOP_MISSIONS_IN_BOUNDS_LIMIT = 25;

    private static final int INTEL_TOP_MISSIONS_FOR_PORTAL_LIMIT = 10;

    @Override
    public Mission importMission(IntelMissionDetails data) {
        return withRecalculation(tracker -> {
            Mission mission = importMissionSummary(data, tracker);
            setMissionAuthor(mission, data.authorName, data.authorFaction);
            setMissionDescription(mission, data.description);
            setMissionNumberCompleted(mission, data.numberCompleted);
            setMissionType(mission, data.type);
            setMissionStepSize(mission, data.steps.size(), tracker);
            List<IntelMissionStep> steps = data.steps;
            for (int i = 0; i < steps.size(); i++) {
                importMissionStep(steps.get(i), mission.getSteps().get(i), tracker);
            }
            setMissionOnline(mission, null, tracker);
            entityManager.persist(mission);
            return mission;
        });
    }

    private void importMissionStep(IntelMissionStep intelMissionStep, MissionStep missionStep,
                                   RecalculationTracker tracker) {
        if (intelMissionStep.hidden) {
            setStepObjective(missionStep, null);
            setStepPoi(missionStep, null, tracker);
        } else {
            POI poi = importPoiById(intelMissionStep.id);
            setPoiType(poi, intelMissionStep.type, tracker);
            if (intelMissionStep.type != POIType.unavailable) {
                setPoiLatitude(poi, fromE6(intelMissionStep.latitudeE6), tracker);
                setPoiLongitude(poi, fromE6(intelMissionStep.longitudeE6), tracker);
                setPoiPicture(poi, intelMissionStep.picture);
                setPoiTitle(poi, intelMissionStep.title);
            }
            entityManager.persist(poi);
            setStepObjective(missionStep, intelMissionStep.objective);
            setStepPoi(missionStep, poi, tracker);
        }
    }

    @Override
    public Collection<Mission> importTopMissionsInBounds(IntelTopMissionsInBounds data) {
        return withRecalculation(tracker -> {
            Collection<Mission> missions = importMissionSummaries(data.summaries, tracker);
            if (missions.size() < INTEL_TOP_MISSIONS_IN_BOUNDS_LIMIT) {
                setMissionsOfflineInBounds(fromE6(data.request.southE6), fromE6(data.request.westE6),
                    fromE6(data.request.northE6), fromE6(data.request.eastE6), missions, tracker);
            }
            return missions;
        });
    }

    @Override
    public Collection<Mission> importTopMissionsForPortal(IntelTopMissionsForPortal data) {
        return withRecalculation(tracker -> {
            Collection<Mission> missions = importMissionSummaries(data.summaries, tracker);
            if (missions.size() < INTEL_TOP_MISSIONS_FOR_PORTAL_LIMIT) {
                setMissionsOfflineForPortal(data.request.guid, missions, tracker);
            }
            return missions;
        });
    }

    @Override
    public Collection<Mission> importMissionSummaries(List<IntelMissionSummary> summaries) {
        return withRecalculation(tracker -> {
            return importMissionSummaries(summaries, tracker);
        });
    }

    private void setMissionsOfflineInBounds(double minLatitude, double minLongitude, double maxLatitude,
                                            double maxLongitude, Collection<Mission> exclude,
                                            RecalculationTracker tracker) {
        TypedQuery<Mission> query = entityManager.createQuery("SELECT m FROM Mission m WHERE m NOT IN :exclude"
            + " AND m.steps[0].poi.latitude BETWEEN :minLatitude AND :maxLatitude"
            + " AND m.steps[0].poi.longitude BETWEEN :minLongitude AND :maxLongitude", Mission.class);
        query.setParameter("exclude", exclude);
        query.setParameter("minLatitude", minLatitude);
        query.setParameter("minLongitude", minLongitude);
        query.setParameter("maxLatitude", maxLatitude);
        query.setParameter("maxLongitude", maxLongitude);
        setMissionsOffline(query.getResultList(), tracker);
    }

    private void setMissionsOfflineForPortal(String startPoiId, Collection<Mission> exclude,
                                             RecalculationTracker tracker) {
        TypedQuery<Mission> query = entityManager.createQuery(
            "SELECT m FROM Mission m WHERE m NOT IN :exclude AND m.steps[0].poi.id = :startPoiId", Mission.class);
        query.setParameter("exclude", exclude);
        query.setParameter("startPoiId", startPoiId);
        setMissionsOffline(query.getResultList(), tracker);
    }

    private void setMissionsOffline(List<Mission> missions, RecalculationTracker tracker) {
        for (Mission mission : missions) {
            setMissionOnline(mission, false, tracker);
        }
    }

    private Collection<Mission> importMissionSummaries(List<IntelMissionSummary> summaries,
                                                       RecalculationTracker tracker) {
        List<Mission> imported = new ArrayList<>();
        for (IntelMissionSummary summary : summaries) {
            Mission mission = importMissionSummary(summary, tracker);
            setMissionOnline(mission, true, tracker);
            entityManager.persist(mission);
            imported.add(mission);
        }
        return imported;
    }

    private Mission importMissionSummary(IntelMissionSummary data, RecalculationTracker tracker) {
        Mission mission = importMissionById(data.id);
        setMissionAverageDurationMilliseconds(mission, data.averageDurationMilliseconds);
        setMissionPicture(mission, data.picture, tracker);
        setMissionRating(mission, fromE6(data.ratingE6));
        setMissionTitle(mission, data.title);
        return mission;
    }

    private static Double fromE6(Integer e6) {
        return e6 == null ? null : e6 / 1_000_000d;
    }
}
