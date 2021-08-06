package com.bannergress.backend.services.impl;

import com.bannergress.backend.dto.CreatorGetMissionForProfile;
import com.bannergress.backend.dto.CreatorGetMissionsList;
import com.bannergress.backend.dto.CreatorMission;
import com.bannergress.backend.dto.CreatorPoi;
import com.bannergress.backend.entities.Mission;
import com.bannergress.backend.entities.MissionStep;
import com.bannergress.backend.entities.POI;
import com.bannergress.backend.enums.CreatorMissionStatus;
import com.bannergress.backend.enums.MissionStatus;
import com.bannergress.backend.enums.POIType;
import com.bannergress.backend.services.CreatorImportService;
import com.google.common.base.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;

import java.util.List;
import java.util.Optional;

/**
 * Service for mission creator imports.
 */
@Service
@Transactional
public class CreatorImportServiceImpl extends BaseImportServiceImpl implements CreatorImportService {
    private static final String ERROR_MISSION_NOT_FOUND = "Mission Not Found";

    @Override
    public void importGetMissionForProfile(@Valid CreatorGetMissionForProfile data) {
        withRecalculation(tracker -> {
            String id = data.request.mission_guid;
            if (data.response.mat_error == null) {
                Mission mission = importMission(id, data.response.mission, data.response.pois, tracker);
                setMissionStatus(mission, MissionStatus.published, tracker);
                entityManager.persist(mission);
            } else {
                if (ERROR_MISSION_NOT_FOUND.equals(data.response.mat_error.title)) {
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
                    if (!Strings.isNullOrEmpty(creatorMission.mission_guid)
                        && (creatorMission.state == CreatorMissionStatus.PUBLISHED
                            || creatorMission.state == CreatorMissionStatus.DISABLED)) {
                        Mission mission = importMission(creatorMission.mission_guid, creatorMission, List.of(),
                            tracker);
                        setMissionStatus(mission, toMissionStatus(creatorMission.state), tracker);
                        entityManager.persist(mission);
                    }
                }
            }
        });
    }

    private static MissionStatus toMissionStatus(CreatorMissionStatus creatorStatus) {
        switch (creatorStatus) {
            case SUBMITTED:
                return MissionStatus.submitted;
            case PUBLISHED:
                return MissionStatus.published;
            case DISABLED:
                return MissionStatus.disabled;
            default:
                throw new IllegalArgumentException(creatorStatus.toString());
        }
    }

    private Mission importMission(String id, CreatorMission creatorMission, List<CreatorPoi> creatorPois,
                                  RecalculationTracker tracker) {
        Mission mission = importMissionById(id);
        setMissionAuthor(mission, creatorMission.definition.author_nickname, null);
        setMissionAverageDurationMilliseconds(mission, creatorMission.stats.median_completion_time);
        setMissionDescription(mission, creatorMission.definition.description);
        setMissionNumberCompleted(mission, creatorMission.stats.num_completed);
        setMissionPicture(mission, creatorMission.definition.logo_url, tracker);
        setMissionRating(mission, creatorMission.stats.rating / 100d);
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
            setPoiLatitude(poi, creatorPoi.get().location.latitude, tracker);
            setPoiLongitude(poi, creatorPoi.get().location.longitude, tracker);
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
