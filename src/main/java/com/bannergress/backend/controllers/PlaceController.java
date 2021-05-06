package com.bannergress.backend.controllers;

import com.bannergress.backend.dto.PlaceDto;
import com.bannergress.backend.entities.Place;
import com.bannergress.backend.entities.PlaceInformation;
import com.bannergress.backend.enums.PlaceType;
import com.bannergress.backend.services.PlaceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public List<PlaceDto> list(@RequestParam(required = false) final PlaceType type,
                               @RequestParam(required = false) final String parentPlaceId,
                               @RequestParam(required = false) final String query) {
        Collection<Place> usedPlaces = placeService.findUsedPlaces(Optional.ofNullable(parentPlaceId),
            Optional.ofNullable(query), Optional.ofNullable(type));
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

    private PlaceDto toDetails(Place place) {
        PlaceDto placeDto = toSummary(place);
        if (place.getParentPlace() != null) {
            placeDto.parentPlace = toDetails(place.getParentPlace());
        }
        return placeDto;
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
