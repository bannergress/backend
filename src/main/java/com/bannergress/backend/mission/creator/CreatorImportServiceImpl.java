package com.bannergress.backend.mission.creator;

import com.bannergress.backend.mission.BaseImportServiceImpl;
import com.bannergress.backend.mission.Mission;
import com.bannergress.backend.mission.MissionStatus;
import com.bannergress.backend.mission.step.MissionStep;
import com.bannergress.backend.poi.POI;
import com.bannergress.backend.poi.POIType;
import com.google.common.base.Strings;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for mission creator imports.
 */
@Service
@Transactional
public class CreatorImportServiceImpl extends BaseImportServiceImpl implements CreatorImportService {
    @Override
    public void importGetMissionForProfile(@Valid CreatorGetMissionForProfile data) {
        withRecalculation(tracker -> {
            String id = data.request.mission_guid;
            if (data.response.mat_error == null) {
                Mission mission = importMission(id, data.response.mission, data.response.pois, tracker);
                setMissionStatus(mission, MissionStatus.published, tracker);
                entityManager.persist(mission);
            } else {
                if (CreatorGetMissionForProfile.ErrorTitle.missionNotFound == data.response.mat_error.title) {
                    Mission mission = entityManager.find(Mission.class, id);
                    if (mission != null) {
                        setMissionStatus(mission, MissionStatus.disabled, tracker);
                    }
                }
            }
        });
    }

    @Override
    public void importGetMissionsList(@Valid CreatorGetMissionsList data) {
        withRecalculation(tracker -> {
            for (List<CreatorMission> list : data.missionLists) {
                for (CreatorMission creatorMission : list) {
                    if (!Strings.isNullOrEmpty(creatorMission.mission_guid)) {
                        if (creatorMission.state == CreatorMissionStatus.PUBLISHED) {
                            // Published version can always be imported
                            Mission mission = importMission(creatorMission.mission_guid, creatorMission, List.of(),
                                tracker);
                            setMissionStatus(mission, MissionStatus.published, tracker);
                        } else if (list.size() == 1) {
                            // All other versions can only be imported if there is no second (published) item
                            Mission mission = entityManager.find(Mission.class, creatorMission.mission_guid);
                            if (mission == null) {
                                mission = importMission(creatorMission.mission_guid, creatorMission, List.of(),
                                    tracker);
                                mission.setStatus(MissionStatus.submitted);
                                entityManager.persist(mission);
                            } else if (mission.getStatus() == MissionStatus.published) {
                                setMissionStatus(mission, MissionStatus.disabled, tracker);
                            }
                        }
                    }
                }
            }
        });
    }

    private Mission importMission(String id, CreatorMission creatorMission, List<CreatorPoi> creatorPois,
                                  RecalculationTracker tracker) {
        Mission mission = importMissionById(id);
        setMissionAuthor(mission, creatorMission.definition.author_nickname, null);
        setMissionDescription(mission, creatorMission.definition.description);
        if (creatorMission.stats != null) {
            setMissionAverageDurationMilliseconds(mission, creatorMission.stats.median_completion_time);
            setMissionNumberCompleted(mission, creatorMission.stats.num_completed);
            setMissionRating(mission, creatorMission.stats.rating / 100d);
        }
        setMissionPicture(mission, creatorMission.definition.logo_url, tracker);
        setMissionTitle(mission, creatorMission.definition.name);
        setMissionType(mission, creatorMission.definition.mission_type);
        if (creatorMission.definition.waypoints != null) {
            setMissionStepSize(mission, creatorMission.definition.waypoints.size(), tracker);
            for (int i = 0; i < creatorMission.definition.waypoints.size(); i++) {
                MissionStep step = mission.getSteps().get(i);
                CreatorMission.Waypoint creatorStep = creatorMission.definition.waypoints.get(i);
                if (creatorStep.hidden) {
                    setStepObjective(step, null);
                    setStepPoi(step, null, tracker);
                } else {
                    setStepObjective(step, creatorStep.objective.type);
                    POI poi = importPoi(creatorStep.poi_guid,
                        creatorPois.stream().filter(p -> p.guid.equals(creatorStep.poi_guid)).findAny(), tracker);
                    setStepPoi(step, poi, tracker);
                }
            }
        }
        return mission;
    }

    private POI importPoi(String id, Optional<CreatorPoi> creatorPoi, RecalculationTracker tracker) {
        POI poi = importPoiById(id);
        if (creatorPoi.isPresent()) {
            setPoiPoint(poi,
                spatial.createPoint(creatorPoi.get().location.latitude, creatorPoi.get().location.longitude), tracker);
            setPoiPicture(poi, creatorPoi.get().imageUrl);
            setPoiTitle(poi, creatorPoi.get().title);
            setPoiType(poi, creatorPoi.get().type, tracker);
        } else {
            setPoiType(poi, POIType.unavailable, tracker);
        }
        entityManager.persist(poi);
        return poi;
    }
}
