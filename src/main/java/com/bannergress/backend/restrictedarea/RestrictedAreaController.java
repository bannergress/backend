package com.bannergress.backend.restrictedarea;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST endpoint for restricted areas.
 */
@RestController
@Hidden
public class RestrictedAreaController {
    @Autowired
    private RestrictedAreaService service;

    @Autowired
    private RestrictedAreaSuggestionService suggestionService;

    @GetMapping("/areas/suggestions")
    public List<RestrictedAreaSuggestion> get(@RequestParam double latitude, @RequestParam double longitude) {
        return suggestionService.getSuggestions(latitude, longitude);
    }

    @GetMapping("/areas/suggestions/{id}")
    public RestrictedAreaDto get(@PathVariable String id) {
        return toDetails(suggestionService.getTemplate(id));
    }

    @GetMapping("/areas/{id}")
    public ResponseEntity<RestrictedAreaDto> get(@PathVariable UUID id) {
        Optional<RestrictedArea> area = service.findByUuid(id);
        return ResponseEntity.of(area.map(this::toDetails));
    }

    @GetMapping("/bnrs/{bannerId}/areas")
    public List<RestrictedAreaDto> getRelevantAreas(@PathVariable String bannerId) {
        return service.findByBannerSlug(bannerId).stream() //
            .map(this::toDetails) //
            .toList();
    }

    private RestrictedAreaDto toDetails(RestrictedArea area) {
        RestrictedAreaDto result = new RestrictedAreaDto();
        result.setId(area.getUuid());
        result.setTitle(area.getTitle());
        result.setArea(area.getArea());
        result.setIngressRelevantArea(area.getIngressRelevantArea());
        result.setRestrictions(area.getRestrictions());
        return result;
    }
}
