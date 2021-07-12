package com.bannergress.backend.controllers;

import com.bannergress.backend.dto.RestrictedAreaDto;
import com.bannergress.backend.entities.RestrictedArea;
import com.bannergress.backend.services.RestrictedAreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URL;
import java.util.Optional;
import java.util.UUID;

/**
 * REST endpoint for restricted areas.
 */
@RestController
public class RestrictedAreaController {
    @Autowired
    private RestrictedAreaService service;

    @GetMapping("/area/{id}")
    public ResponseEntity<RestrictedAreaDto> get(@PathVariable UUID id) {
        Optional<RestrictedArea> area = service.findByUuid(id);
        return ResponseEntity.of(area.map(this::toDetails));
    }

    private RestrictedAreaDto toDetails(RestrictedArea area) {
        RestrictedAreaDto result = new RestrictedAreaDto();
        result.id = area.getUuid();
        result.title = area.getTitle();
        result.area = area.getArea();
        result.generalRestriction = toRestriction(area.isGeneralRestriction(), area.getGeneralRestrictionDescription(),
            area.getGeneralRestrictionUrl());
        result.timedRestriction = toRestriction(area.isTimedRestriction(), area.getTimedRestrictionDescription(),
            area.getTimedRestrictionUrl());
        result.monetaryRestriction = toRestriction(area.isMonetaryRestriction(),
            area.getMonetaryRestrictionDescription(), area.getMonetaryRestrictionUrl());
        result.startDate = area.getStartDate();
        result.endDate = area.getEndDate();
        return result;
    }

    private RestrictedAreaDto.Restriction toRestriction(boolean restricted, String description, URL url) {
        if (restricted) {
            RestrictedAreaDto.Restriction result = new RestrictedAreaDto.Restriction();
            result.description = description;
            result.url = url;
            return result;
        } else {
            return null;
        }
    }
}
