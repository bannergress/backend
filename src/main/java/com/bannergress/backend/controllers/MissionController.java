package com.bannergress.backend.controllers;

import com.bannergress.backend.dto.*;
import com.bannergress.backend.entities.Mission;
import com.bannergress.backend.entities.MissionStep;
import com.bannergress.backend.entities.NamedAgent;
import com.bannergress.backend.entities.POI;
import com.bannergress.backend.enums.MissionSortOrder;
import com.bannergress.backend.security.Roles;
import com.bannergress.backend.services.MissionService;
import com.bannergress.backend.utils.DistanceCalculation;
import com.bannergress.backend.validation.NianticId;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import java.security.Principal;
import java.util.*;

import static com.bannergress.backend.utils.Spatial.getLatitude;
import static com.bannergress.backend.utils.Spatial.getLongitude;

/**
 * REST endpoint for missions.
 */
@RestController
@Validated
public class MissionController {
    @Autowired
    MissionService missionService;

    @RolesAllowed(Roles.CREATE_BANNER)
    @GetMapping("/missions/unused")
    public Collection<MissionDto> getUnused(@RequestParam @NotEmpty String query,
                                            @RequestParam final Optional<MissionSortOrder> orderBy,
                                            @RequestParam(defaultValue = "ASC") final Direction orderDirection,
                                            @RequestParam(defaultValue = "0") final int offset,
                                            @RequestParam(defaultValue = "20") @Max(100) final int limit,
                                            Principal principal) {
        Collection<Mission> unusedMissions = missionService.findUnusedMissions(query, orderBy, orderDirection, offset,
            limit);
        return Collections2.transform(unusedMissions, mission -> toSummaryForUnused(mission, principal));
    }

    @PostMapping("/missions/status")
    public Map<String, MissionStatusDto> getStatus(@RequestBody Collection<@NianticId @NotNull String> ids) {
        Collection<Mission> missions = missionService.findByIds(ids);
        Map<String, MissionStatusDto> result = new HashMap<>();
        result.putAll(Maps.toMap(ids, id -> new MissionStatusDto()));
        for (Mission mission : missions) {
            MissionStatusDto status = result.get(mission.getId());
            status.latestUpdateSummary = mission.getLatestUpdateSummary();
            status.latestUpdateDetails = mission.getLatestUpdateDetails();
        }
        return result;
    }

    @GetMapping("/missions/{id}")
    public MissionDto get(@PathVariable String id, Principal principal) {
        Optional<Mission> mission = missionService.findById(id);
        if (mission.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else {
            return toDetails(mission.get(), principal);
        }
    }

    private static MissionDto toSummary(Mission mission, Principal principal) {
        MissionDto dto = new MissionDto();
        dto.id = mission.getId();
        dto.title = mission.getTitle();
        dto.picture = mission.getPicture();
        return dto;
    }

    public static MissionDto toSummaryForUnused(Mission mission, Principal principal) {
        MissionDto dto = toSummary(mission, principal);
        dto.description = mission.getDescription();
        dto.author = mission.getAuthor() == null ? null : toAgentSummary(mission.getAuthor(), principal);
        return dto;
    }

    public static NamedAgentDto toAgentSummary(NamedAgent agent, Principal principal) {
        if (BannerController.getAgent(principal).isEmpty()) {
            return null;
        }
        NamedAgentDto result = new NamedAgentDto();
        result.name = agent.getName();
        result.faction = agent.getFaction();
        return result;
    }

    public static MissionDto toDetails(Mission mission, Principal principal) {
        MissionDto dto = toSummary(mission, principal);
        dto.steps = Lists.transform(mission.getSteps(), MissionController::toMissionStepDetails);
        dto.description = mission.getDescription();
        dto.type = mission.getType();
        dto.status = mission.getStatus();
        dto.author = mission.getAuthor() == null ? null : toAgentSummary(mission.getAuthor(), principal);
        dto.averageDurationMilliseconds = mission.getAverageDurationMilliseconds();
        dto.lengthMeters = DistanceCalculation.calculateLengthMeters(List.of(mission));
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
        poiDto.latitude = getLatitude(poi.getPoint());
        poiDto.longitude = getLongitude(poi.getPoint());
        poiDto.picture = poi.getPicture();
        poiDto.title = poi.getTitle();
        poiDto.type = poi.getType();
        return poiDto;
    }
}
