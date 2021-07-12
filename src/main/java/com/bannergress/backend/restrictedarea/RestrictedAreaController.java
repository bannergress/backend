package com.bannergress.backend.restrictedarea;

import com.bannergress.backend.security.Roles;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST endpoint for restricted areas.
 */
@RestController
@Hidden
@Validated
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

    @RolesAllowed(Roles.MANAGE_AREAS)
    @PostMapping("/areas")
    public ResponseEntity<RestrictedAreaDto> post(@Valid @RequestBody RestrictedAreaDto area) {
        UUID id = service.create(area);
        return get(id);
    }

    @RolesAllowed(Roles.MANAGE_AREAS)
    @PutMapping("/areas/{id}")
    @Hidden
    public ResponseEntity<RestrictedAreaDto> put(@PathVariable final UUID id,
                                                 @Valid @RequestBody RestrictedAreaDto area) {
        service.update(id, area);
        return get(id);
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
