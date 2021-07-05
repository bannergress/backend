package com.bannergress.backend.controllers;

import com.bannergress.backend.dto.*;
import com.bannergress.backend.entities.Mission;
import com.bannergress.backend.security.Roles;
import com.bannergress.backend.services.IntelImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST endpoint for imports.
 */
@RestController
public class ImportController {
    @Autowired
    private IntelImportService intelImportService;

    @RolesAllowed(Roles.IMPORT_DATA)
    @PostMapping("/import/topMissionsInBounds")
    public Map<String, MissionStatus> importTopMissionsInBounds(@RequestBody @Valid IntelTopMissionsInBounds data) {
        Collection<Mission> missions = intelImportService.importTopMissionsInBounds(data);
        return toStatusMap(missions);
    }

    @RolesAllowed(Roles.IMPORT_DATA)
    @PostMapping("/import/topMissionsForPortal")
    public Map<String, MissionStatus> importTopMissionsForPortal(@RequestBody @Valid IntelTopMissionsForPortal data) {
        Collection<Mission> missions = intelImportService.importTopMissionsForPortal(data);
        return toStatusMap(missions);
    }

    @RolesAllowed(Roles.IMPORT_DATA)
    @PostMapping("/import/details")
    public Map<String, MissionStatus> importMissionDetails(@RequestBody @Valid IntelMissionDetails data) {
        Mission mission = intelImportService.importMission(data);
        return toStatusMap(List.of(mission));
    }

    @RolesAllowed(Roles.IMPORT_DATA)
    @PostMapping("/import/summaries")
    public Map<String, MissionStatus> importMissionSummaries(@RequestBody List<@Valid IntelMissionSummary> summaries) {
        Collection<Mission> missions = intelImportService.importMissionSummaries(summaries);
        return toStatusMap(missions);
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
