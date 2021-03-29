package com.bannergress.backend.controllers;

import java.util.Collection;
import java.util.Optional;

import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.NotEmpty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.bannergress.backend.dto.MissionDto;
import com.bannergress.backend.dto.MissionStepDto;
import com.bannergress.backend.dto.PoiDto;
import com.bannergress.backend.entities.Mission;
import com.bannergress.backend.entities.MissionStep;
import com.bannergress.backend.entities.POI;
import com.bannergress.backend.security.Roles;
import com.bannergress.backend.services.MissionService;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

/** REST endpoint for missions. */
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
