package com.bannergress.backend.controllers;

import com.bannergress.backend.dto.IntelMissionDetails;
import com.bannergress.backend.dto.IntelTopMissionsInBounds;
import com.bannergress.backend.security.Roles;
import com.bannergress.backend.services.MissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;

/**
 * REST endpoint for imports.
 */
@RestController
public class ImportController {
    @Autowired
    private MissionService importService;

    @RolesAllowed(Roles.IMPORT_DATA)
    @PostMapping("/importTopMissionsInBounds")
    public void importTopMissionsInBounds(@RequestBody @Valid IntelTopMissionsInBounds data) {
        importService.importTopMissionsInBounds(data);
    }

    @RolesAllowed(Roles.IMPORT_DATA)
    @PostMapping("/import")
    public void importMissionDetails(@RequestBody @Valid IntelMissionDetails data) {
        importService.importMission(data);
    }
}
