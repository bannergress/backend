package com.bannergress.backend.services.impl;

import com.bannergress.backend.entities.Place;
import com.bannergress.backend.entities.PlaceInformation;
import com.bannergress.backend.enums.PlaceType;
import com.bannergress.backend.services.GeocodingService;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Geocoding using Google Maps API.
 */
@Service
@Transactional
@Profile("googlemaps")
public class GoogleMapsGeocodingServiceImpl implements GeocodingService {

    private static final String DEFAULT_LANGUAGE = "en";

    private final GeoApiContext apiContext;

    @Autowired
    private EntityManager entityManager;

    public GoogleMapsGeocodingServiceImpl(@Value("${google.api-key}") String apiKey) {
        apiContext = new GeoApiContext.Builder().apiKey(apiKey).build();
    }

    @Override
    public List<Place> getPlaces(double latitude, double longitude) {
        try {
            GeocodingResult[] geocodingResults = GeocodingApi //
                .reverseGeocode(apiContext, new LatLng(latitude, longitude)) //
                .resultType(AddressType.COUNTRY, AddressType.ADMINISTRATIVE_AREA_LEVEL_1, AddressType.LOCALITY)
                .language(DEFAULT_LANGUAGE) //
                .await();
            List<Place> result = new ArrayList<>();
            for (GeocodingResult geocodingResult : geocodingResults) {
                for (AddressType addressType : geocodingResult.types) {
                    PlaceType placeType = mapAddressType(addressType);
                    if (placeType != null) {
                        Place place = importPlace(geocodingResult, placeType, DEFAULT_LANGUAGE);
                        result.add(place);
                        break;
                    }
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Place importPlace(GeocodingResult geocodingResult, PlaceType type, String languageCode) {
        Place result = entityManager.find(Place.class, geocodingResult.placeId);
        if (result == null) {
            result = new Place();
            result.setId(geocodingResult.placeId);
            result.setType(type);
            entityManager.persist(result);
        }
        importPlaceInformation(result, geocodingResult, languageCode);
        return result;
    }

    private PlaceInformation importPlaceInformation(Place place, GeocodingResult geocodingResult, String languageCode) {
        PlaceInformation result = null;
        for (PlaceInformation placeInformation : place.getInformation()) {
            if (placeInformation.getLanguageCode().equals(languageCode)) {
                result = placeInformation;
                break;
            }
        }
        if (result == null) {
            result = new PlaceInformation();
            result.setPlace(place);
            result.setLanguageCode(languageCode);
        }
        result.setFormattedAddress(geocodingResult.formattedAddress);
        for (AddressComponent addressComponent : geocodingResult.addressComponents) {
            for (AddressComponentType type : addressComponent.types) {
                if (isRelevantAddressComponentType(place.getType(), type)) {
                    result.setLongName(addressComponent.longName);
                    result.setShortName(addressComponent.shortName);
                    entityManager.persist(result);
                    return result;
                }
            }
        }
        throw new AssertionError();
    }

    private PlaceType mapAddressType(AddressType addressType) {
        switch (addressType) {
            case ADMINISTRATIVE_AREA_LEVEL_1:
                return PlaceType.administrative_area_level_1;
            case ADMINISTRATIVE_AREA_LEVEL_2:
                return PlaceType.administrative_area_level_2;
            case ADMINISTRATIVE_AREA_LEVEL_3:
                return PlaceType.administrative_area_level_3;
            case ADMINISTRATIVE_AREA_LEVEL_4:
                return PlaceType.administrative_area_level_4;
            case ADMINISTRATIVE_AREA_LEVEL_5:
                return PlaceType.administrative_area_level_5;
            case COUNTRY:
                return PlaceType.country;
            case LOCALITY:
                return PlaceType.locality;
            default:
                return null;
        }
    }

    private boolean isRelevantAddressComponentType(PlaceType placeType, AddressComponentType addressComponentType) {
        switch (placeType) {
            case administrative_area_level_1:
                return addressComponentType == AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_1;
            case administrative_area_level_2:
                return addressComponentType == AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_2;
            case administrative_area_level_3:
                return addressComponentType == AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_3;
            case administrative_area_level_4:
                return addressComponentType == AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_4;
            case administrative_area_level_5:
                return addressComponentType == AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_5;
            case country:
                return addressComponentType == AddressComponentType.COUNTRY;
            case locality:
                return addressComponentType == AddressComponentType.LOCALITY;
        }
        throw new AssertionError();
    }

    @Override
    public PlaceInformation getPlaceInformation(Place place, String language) {
        return place.getInformation().get(0);
    }
}
