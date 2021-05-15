package com.bannergress.backend.controllers;

import com.bannergress.backend.dto.*;
import com.bannergress.backend.entities.Mission;
import com.bannergress.backend.entities.MissionStep;
import com.bannergress.backend.entities.NamedAgent;
import com.bannergress.backend.entities.POI;
import com.bannergress.backend.enums.MissionSortOrder;
import com.bannergress.backend.security.Roles;
import com.bannergress.backend.services.MissionService;
import com.bannergress.backend.validation.NianticId;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.keycloak.KeycloakPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
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
                                            @RequestParam(defaultValue = "20") @Max(100) final int limit) {
        Collection<Mission> unusedMissions = missionService.findUnusedMissions(query, orderBy, orderDirection, offset,
            limit);
        return Collections2.transform(unusedMissions, MissionController::toSummaryForUnused);
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

    public static MissionDto toSummaryForUnused(Mission mission) {
        MissionDto dto = toSummary(mission);
        dto.description = mission.getDescription();
        dto.author = mission.getAuthor() == null ? null : toAgentSummary(mission.getAuthor());
        return dto;
    }

    public static NamedAgentDto toAgentSummary(NamedAgent agent) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof KeycloakPrincipal)) {
            return null;
        }
        NamedAgentDto result = new NamedAgentDto();
        result.name = agent.getName();
        result.faction = agent.getFaction();
        return result;
    }

    public static MissionDto toDetails(Mission mission) {
        MissionDto dto = toSummary(mission);
        dto.steps = Lists.transform(mission.getSteps(), MissionController::toMissionStepDetails);
        dto.description = mission.getDescription();
        dto.type = mission.getType();
        dto.online = mission.isOnline();
        dto.author = mission.getAuthor() == null ? null : toAgentSummary(mission.getAuthor());
        dto.averageDurationMilliseconds = mission.getAverageDurationMilliseconds();
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
