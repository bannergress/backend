package com.bannergress.backend.place.geocoding;

import com.bannergress.backend.place.Place;
import com.bannergress.backend.place.PlaceInformation;
import com.bannergress.backend.place.PlaceType;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Geocoding using Google Maps API.
 */
@Service
class GoogleMapsGeocodingServiceImpl implements GeocodingService {

    private static final String DEFAULT_LANGUAGE = "en";

    private final GeoApiContext apiContext;

    public GoogleMapsGeocodingServiceImpl(@Value("${google.api-key}") String apiKey) {
        apiContext = new GeoApiContext.Builder().apiKey(apiKey).build();
    }

    @Override
    public Set<Place> getPlaces(double latitude, double longitude) {
        try {
            GeocodingResult[] geocodingResults = GeocodingApi //
                .reverseGeocode(apiContext, new LatLng(latitude, longitude)) //
                .resultType(AddressType.COUNTRY, AddressType.ADMINISTRATIVE_AREA_LEVEL_1,
                    AddressType.ADMINISTRATIVE_AREA_LEVEL_2, AddressType.ADMINISTRATIVE_AREA_LEVEL_3,
                    AddressType.ADMINISTRATIVE_AREA_LEVEL_4, AddressType.ADMINISTRATIVE_AREA_LEVEL_5,
                    AddressType.LOCALITY)
                .language(DEFAULT_LANGUAGE) //
                .await();
            Set<Place> result = new HashSet<>();
            for (GeocodingResult geocodingResult : geocodingResults) {
                if (isColloquialResult(geocodingResult)) {
                    continue;
                }
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

    private boolean isColloquialResult(GeocodingResult geocodingResult) {
        return Arrays.stream(geocodingResult.types).anyMatch(AddressType.COLLOQUIAL_AREA::equals);
    }

    private Place importPlace(GeocodingResult geocodingResult, PlaceType type, String languageCode) {
        Place result = new Place();
        result.setId(geocodingResult.placeId);
        result.setType(type);
        result.setBoundaryMinLatitude(geocodingResult.geometry.viewport.southwest.lat);
        result.setBoundaryMinLongitude(geocodingResult.geometry.viewport.southwest.lng);
        result.setBoundaryMaxLatitude(geocodingResult.geometry.viewport.northeast.lat);
        result.setBoundaryMaxLongitude(geocodingResult.geometry.viewport.northeast.lng);
        importPlaceInformation(result, geocodingResult, languageCode);
        return result;
    }

    private PlaceInformation importPlaceInformation(Place place, GeocodingResult geocodingResult, String languageCode) {
        PlaceInformation result = new PlaceInformation();
        result.setPlace(place);
        place.getInformation().add(result);
        result.setLanguageCode(languageCode);
        result.setFormattedAddress(geocodingResult.formattedAddress);
        for (AddressComponent addressComponent : geocodingResult.addressComponents) {
            for (AddressComponentType type : addressComponent.types) {
                if (isRelevantAddressComponentType(place.getType(), type)) {
                    result.setLongName(addressComponent.longName);
                    result.setShortName(addressComponent.shortName);
                    return result;
                }
            }
        }
        result.setLongName(geocodingResult.formattedAddress);
        result.setShortName(geocodingResult.formattedAddress);
        return result;
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
