package com.bannergress.backend.controllers;

import com.bannergress.backend.dto.PlaceDto;
import com.bannergress.backend.entities.Place;
import com.bannergress.backend.entities.PlaceInformation;
import com.bannergress.backend.enums.PlaceType;
import com.bannergress.backend.services.PlaceService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
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
@CrossOrigin
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
        return usedPlaces.stream().map(this::toDetails).collect(Collectors.toList());
    }

    private PlaceDto toDetails(Place place) {
        PlaceInformation information = placeService.getPlaceInformation(place, "en");
        PlaceDto placeDto = new PlaceDto();
        placeDto.id = place.getId();
        placeDto.formattedAddress = information.getFormattedAddress();
        placeDto.longName = information.getLongName();
        placeDto.shortName = information.getShortName();
        return placeDto;
    }
}