package com.bannergress.backend.mission;

import com.bannergress.backend.agent.NamedAgent;
import com.bannergress.backend.agent.NamedAgentDto;
import com.bannergress.backend.mission.step.MissionStep;
import com.bannergress.backend.mission.step.MissionStepDto;
import com.bannergress.backend.mission.validation.NianticId;
import com.bannergress.backend.poi.POI;
import com.bannergress.backend.poi.PoiDto;
import com.bannergress.backend.security.Roles;
import com.bannergress.backend.spatial.DistanceCalculation;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static com.bannergress.backend.spatial.Spatial.getLatitude;
import static com.bannergress.backend.spatial.Spatial.getLongitude;

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
        Collection<Mission> unusedMissions = missionService.findUnusedMissions(query.trim(), orderBy, orderDirection, offset,
            limit);
        return Collections2.transform(unusedMissions, MissionController::toSummaryForUnused);
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
        if (!(authentication instanceof JwtAuthenticationToken)) {
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
        dto.status = mission.getStatus();
        dto.author = mission.getAuthor() == null ? null : toAgentSummary(mission.getAuthor());
        dto.averageDurationMilliseconds = mission.getAverageDurationMilliseconds();
        dto.lengthMeters = DistanceCalculation.calculateLengthMeters(List.of(mission));
        dto.latestUpdateStatus = mission.getLatestUpdateStatus();
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
