package com.bannergress.backend.controllers;

import com.bannergress.backend.dto.PlaceDto;
import com.bannergress.backend.entities.Place;
import com.bannergress.backend.entities.PlaceInformation;
import com.bannergress.backend.enums.PlaceSortOrder;
import com.bannergress.backend.enums.PlaceType;
import com.bannergress.backend.security.Roles;
import com.bannergress.backend.services.PlaceService;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * REST endpoint for places.
 */
@RestController
public class PlaceController {
    private final PlaceService placeService;

    public PlaceController(PlaceService placeService) {
        this.placeService = placeService;
    }

    /**
     * Lists places used by at least one banner.
     *
     * @param minLatitude  Minimum latitude of the bounding box.
     * @param maxLatitude  Maximum latitude of the bounding box.
     * @param minLongitude Minimum longitude of the bounding box.
     * @param maxLongitude Maximum longitude of the bounding box.
     * @return Banners.
     */
    @GetMapping(value = "/places", params = {"used=true"})
    public List<PlaceDto> list(@RequestParam final Optional<PlaceType> type,
                               @RequestParam final Optional<String> parentPlaceId,
                               @RequestParam final Optional<String> query,
                               @RequestParam(defaultValue = "numberOfBanners") final PlaceSortOrder orderBy,
                               @RequestParam(defaultValue = "DESC") final Direction orderDirection,
                               @RequestParam(defaultValue = "0") final int offset,
                               @RequestParam final Optional<Integer> limit) {
        Collection<Place> usedPlaces = placeService.findUsedPlaces(parentPlaceId, query, type, orderBy, orderDirection,
            offset, limit);
        return usedPlaces.stream().map(this::toSummary).collect(Collectors.toList());
    }

    /**
     * Gets a place with a specified ID.
     *
     * @param id ID.
     * @return Place.
     */
    @GetMapping("/places/{id}")
    public ResponseEntity<PlaceDto> get(@PathVariable final String id) {
        return ResponseEntity.of(placeService.findPlaceBySlug(id).map(this::toDetails));
    }

    @RolesAllowed(Roles.MANAGE_PLACES)
    @PostMapping("/places/recalculate")
    @Hidden
    public void calculateAllPlaces() {
        placeService.updateAllPlaces();
    }

    private PlaceDto toDetails(Place place) {
        PlaceDto placeDto = toSummary(place);
        collapseParents(place).findFirst().ifPresent(parentPlace -> {
            placeDto.parentPlace = toDetails(parentPlace);
        });
        return placeDto;
    }

    private Stream<Place> collapseParents(Place place) {
        return place.getParentPlaces().stream().flatMap(p -> p.isCollapsed() ? collapseParents(p) : Stream.of(p));
    }

    private PlaceDto toSummary(Place place) {
        PlaceInformation information = placeService.getPlaceInformation(place, "en");
        PlaceDto placeDto = new PlaceDto();
        placeDto.id = place.getSlug();
        placeDto.numberOfBanners = place.getNumberOfBanners();
        placeDto.formattedAddress = information.getFormattedAddress();
        placeDto.longName = information.getLongName();
        placeDto.shortName = information.getShortName();
        placeDto.boundaryMinLatitude = place.getBoundaryMinLatitude();
        placeDto.boundaryMinLongitude = place.getBoundaryMinLongitude();
        placeDto.boundaryMaxLatitude = place.getBoundaryMaxLatitude();
        placeDto.boundaryMaxLongitude = place.getBoundaryMaxLongitude();
        placeDto.type = place.getType();
        return placeDto;
    }
}
