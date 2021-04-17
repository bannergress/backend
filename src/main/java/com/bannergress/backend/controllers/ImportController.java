package com.bannergress.backend.controllers;

import com.bannergress.backend.dto.IntelMissionDetails;
import com.bannergress.backend.dto.IntelTopMissionsForPortal;
import com.bannergress.backend.dto.IntelTopMissionsInBounds;
import com.bannergress.backend.dto.MissionStatus;
import com.bannergress.backend.entities.Mission;
import com.bannergress.backend.security.Roles;
import com.bannergress.backend.services.MissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST endpoint for imports.
 */
@RestController
public class ImportController {
    @Autowired
    private MissionService importService;

    @RolesAllowed(Roles.IMPORT_DATA)
    @PostMapping("/import/topMissionsInBounds")
    public Map<String, MissionStatus> importTopMissionsInBounds(@RequestBody @Valid IntelTopMissionsInBounds data) {
        Collection<Mission> missions = importService.importTopMissionsInBounds(data);
        return toStatusMap(missions);
    }

    @RolesAllowed(Roles.IMPORT_DATA)
    @PostMapping("/import/topMissionsForPortal")
    public Map<String, MissionStatus> importTopMissionsForPortal(@RequestBody @Valid IntelTopMissionsForPortal data) {
        Collection<Mission> missions = importService.importTopMissionsForPortal(data);
        return toStatusMap(missions);
    }

    @RolesAllowed(Roles.IMPORT_DATA)
    @PostMapping("/import/details")
    public MissionStatus importMissionDetails(@RequestBody @Valid IntelMissionDetails data) {
        Mission mission = importService.importMission(data);
        return toMissionStatus(mission);
    }

    private Map<String, MissionStatus> toStatusMap(Collection<Mission> imported) {
        return imported.stream().collect(Collectors.toMap(Mission::getId, ImportController::toMissionStatus));
    }

    private static MissionStatus toMissionStatus(Mission mission) {
        MissionStatus status = new MissionStatus();
        status.latestUpdateDetails = mission.getLatestUpdateDetails();
        status.latestUpdateSummary = mission.getLatestUpdateSummary();
        return status;
    }
}
