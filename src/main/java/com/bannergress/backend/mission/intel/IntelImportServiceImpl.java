package com.bannergress.backend.mission.intel;

import com.bannergress.backend.mission.BaseImportServiceImpl;
import com.bannergress.backend.mission.Mission;
import com.bannergress.backend.mission.MissionStatus;
import com.bannergress.backend.mission.step.MissionStep;
import com.bannergress.backend.poi.POI;
import com.bannergress.backend.poi.POIType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Service for imports from Intel.
 */
@Service
@Transactional
public class IntelImportServiceImpl extends BaseImportServiceImpl implements IntelImportService {
    @Override
    public Mission importMission(IntelMissionDetails data, boolean setMissionOnline) {
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
            setMissionStatus(mission, setMissionOnline ? MissionStatus.published
                : mission.getStatus() == MissionStatus.submitted ? MissionStatus.disabled : null, tracker);
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
                setPoiPoint(poi,
                    spatial.createPoint(fromE6(intelMissionStep.latitudeE6), fromE6(intelMissionStep.longitudeE6)),
                    tracker);
                setPoiPicture(poi, intelMissionStep.picture);
                setPoiTitle(poi, intelMissionStep.title);
            }
            entityManager.persist(poi);
            setStepObjective(missionStep, intelMissionStep.objective);
            setStepPoi(missionStep, poi, tracker);
        }
    }

    @Override
    public Collection<Mission> importMissionSummaries(List<IntelMissionSummary> summaries) {
        return withRecalculation(tracker -> {
            List<Mission> imported = new ArrayList<>();
            for (IntelMissionSummary summary : summaries) {
                Mission mission = importMissionSummary(summary, tracker);
                setMissionStatus(mission, MissionStatus.published, tracker);
                entityManager.persist(mission);
                imported.add(mission);
            }
            return imported;
        });
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
