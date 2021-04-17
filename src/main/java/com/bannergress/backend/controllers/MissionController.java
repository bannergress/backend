package com.bannergress.backend.controllers;

import com.bannergress.backend.dto.MissionDto;
import com.bannergress.backend.dto.MissionStatus;
import com.bannergress.backend.dto.MissionStepDto;
import com.bannergress.backend.dto.PoiDto;
import com.bannergress.backend.entities.Mission;
import com.bannergress.backend.entities.MissionStep;
import com.bannergress.backend.entities.POI;
import com.bannergress.backend.security.Roles;
import com.bannergress.backend.services.MissionService;
import com.bannergress.backend.validation.NianticId;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * REST endpoint for missions.
 */
@RestController
public class MissionController {
    @Autowired
    MissionService missionService;

    @RolesAllowed(Roles.CREATE_BANNER)
    @GetMapping("/missions/unused")
    public Collection<MissionDto> getUnused(@RequestParam @NotEmpty String search) {
        Collection<Mission> unusedMissions = missionService.findUnusedMissions(search, 300);
        return Collections2.transform(unusedMissions, MissionController::toSummary);
    }

    @PostMapping("/missions/status")
    public Map<String, MissionStatus> getStatus(@RequestBody Collection<@NianticId @NotNull String> ids) {
        Collection<Mission> missions = missionService.findByIds(ids);
        Map<String, MissionStatus> result = new HashMap<>();
        result.putAll(Maps.toMap(ids, id -> new MissionStatus()));
        for (Mission mission : missions) {
            MissionStatus status = result.get(mission.getId());
            status.latestUpdateSummary = mission.getLatestUpdateSummary();
            status.latestUpdateDetails = mission.getLatestUpdateDetails();
        }
        return result;
    }

    @GetMapping("/missions/requested")
    public Collection<String> getRequestedMissions(@RequestParam(defaultValue = "10") @Min(1) @Max(100) int amount) {
        return missionService.findNextRequestedMissions(amount);
    }

    @GetMapping("/missions/{id}")
    public MissionDto get(@PathVariable String id) {
        Optional<Mission> mission = missionService.findById(id);
        if (mission.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else {
            return toDetails(mission.get());
        }
    }

    private static MissionDto toSummary(Mission mission) {
        MissionDto dto = new MissionDto();
        dto.id = mission.getId();
        dto.title = mission.getTitle();
        dto.picture = mission.getPicture();
        return dto;
    }

    public static MissionDto toDetails(Mission mission) {
        MissionDto dto = toSummary(mission);
        dto.steps = Lists.transform(mission.getSteps(), MissionController::toMissionStepDetails);
        dto.description = mission.getDescription();
        dto.type = mission.getType();
        dto.online = mission.isOnline();
        return dto;
    }

    private static MissionStepDto toMissionStepDetails(MissionStep step) {
        MissionStepDto stepDto = new MissionStepDto();
        stepDto.objective = step.getObjective();
        POI poi = step.getPoi();
        if (poi != null) {
            stepDto.poi = toPoiDetails(poi);
        }
        return stepDto;
    }

    private static PoiDto toPoiDetails(POI poi) {
        PoiDto poiDto = new PoiDto();
        poiDto.id = poi.getId();
        poiDto.latitude = poi.getLatitude();
        poiDto.longitude = poi.getLongitude();
        poiDto.picture = poi.getPicture();
        poiDto.title = poi.getTitle();
        poiDto.type = poi.getType();
        return poiDto;
    }
}
