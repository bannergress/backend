package com.bannergress.backend.controllers;

import com.bannergress.backend.dto.*;
import com.bannergress.backend.entities.Mission;
import com.bannergress.backend.security.Roles;
import com.bannergress.backend.services.CreatorImportService;
import com.bannergress.backend.services.IntelImportService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    private CreatorImportService creatorImportService;

    @Autowired
    private IntelImportService intelImportService;

    @RolesAllowed(Roles.IMPORT_DATA)
    @PostMapping("/import/details")
    public Map<String, MissionStatusDto> importMissionDetails(@RequestBody @Valid IntelMissionDetails data,
                                                           @RequestParam(defaultValue = "true") boolean setStatusOnline) {
        Mission mission = intelImportService.importMission(data, setStatusOnline);
        return toStatusMap(List.of(mission));
    }

    @RolesAllowed(Roles.IMPORT_DATA)
    @PostMapping("/import/summaries")
    public Map<String, MissionStatusDto> importMissionSummaries(@RequestBody List<@Valid IntelMissionSummary> summaries) {
        Collection<Mission> missions = intelImportService.importMissionSummaries(summaries);
        return toStatusMap(missions);
    }

    @RolesAllowed(Roles.IMPORT_DATA)
    @PostMapping("/import/getMissionForProfile")
    public void importGetMissionForProfile(@RequestBody @Valid CreatorGetMissionForProfile data) {
        creatorImportService.importGetMissionForProfile(data);
    }

    @RolesAllowed(Roles.IMPORT_DATA)
    @PostMapping("/import/getMissionsList")
    public void importGetMissionsList(@RequestBody @Valid CreatorGetMissionsList data) {
        creatorImportService.importGetMissionsList(data);
    }

    private Map<String, MissionStatusDto> toStatusMap(Collection<Mission> imported) {
        return imported.stream().collect(Collectors.toMap(Mission::getId, ImportController::toMissionStatus));
    }

    private static MissionStatusDto toMissionStatus(Mission mission) {
        MissionStatusDto status = new MissionStatusDto();
        status.latestUpdateDetails = mission.getLatestUpdateDetails();
        status.latestUpdateSummary = mission.getLatestUpdateSummary();
        return status;
    }
}
