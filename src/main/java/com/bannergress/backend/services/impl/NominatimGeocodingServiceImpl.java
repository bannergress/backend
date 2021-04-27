package com.bannergress.backend.services.impl;

import com.bannergress.backend.entities.Place;
import com.bannergress.backend.entities.PlaceInformation;
import com.bannergress.backend.enums.PlaceType;
import com.bannergress.backend.services.GeocodingService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.RateLimiter;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Geocoding using Google Maps API.
 */
@Service
@Profile("nominatim")
public class NominatimGeocodingServiceImpl implements GeocodingService {
    private static final String DEFAULT_LANGUAGE = "en";

    private final OkHttpClient client;

    private final RateLimiter rateLimiter;

    private final HttpUrl baseUrl;

    private final ObjectMapper objectMapper;

    public NominatimGeocodingServiceImpl(
        @Value("${nominatim.baseUrl:https://nominatim.openstreetmap.org/}") String baseUrl,
        @Value("${nominatim.userAgent:#{null}}") Optional<String> userAgent) {
        this.baseUrl = HttpUrl.parse(baseUrl);
        client = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                if (userAgent.isPresent()) {
                    return chain.proceed(chain.request().newBuilder().header("User-Agent", userAgent.get()).build());
                } else {
                    return chain.proceed(chain.request());
                }
            }
        }).build();
        rateLimiter = RateLimiter.create(1);
        objectMapper = new ObjectMapper();
    }

    private static final ImmutableMap<Integer, String> acceptedTypes = ImmutableMap.<Integer, String>builder()
        .put(10, "city").put(8, "county").put(5, "state").put(3, "country").build();

    private static final ImmutableMap<Integer, PlaceType> mappedTypes = ImmutableMap.<Integer, PlaceType>builder()
        .put(10, PlaceType.locality).put(8, PlaceType.administrative_area_level_2)
        .put(5, PlaceType.administrative_area_level_1).put(3, PlaceType.country).build();

    @Override
    public Optional<Place> getPlaceHierarchy(double latitude, double longitude) {
        List<Place> result = mappedTypes.keySet().stream().map(zoom -> {
            return queryOnePlace(latitude, longitude, zoom, DEFAULT_LANGUAGE);
        }).flatMap(Optional::stream).collect(Collectors.toList());
        return result.stream().reduce((a, b) -> {
            b.setParentPlace(a);
            return b;
        });
    }

    @Override
    public PlaceInformation getPlaceInformation(Place place, String language) {
        return place.getInformation().get(0);
    }

    private Optional<Place> queryOnePlace(double latitude, double longitude, int zoom, String language) {
        rateLimiter.acquire();
        Request request = createRequest(latitude, longitude, zoom, language);
        try (Response response = client.newCall(request).execute()) {
            NominatimResult apiResult = objectMapper.readValue(response.body().charStream(), NominatimResult.class);
            if (acceptedTypes.get(zoom).equals(apiResult.addresstype)) {
                Place result = new Place();
                result.setId(String.valueOf(apiResult.place_id));
                result.setType(mappedTypes.get(zoom));
                result.setBoundaryMinLatitude(Double.parseDouble(apiResult.boundingbox.get(0)));
                result.setBoundaryMinLongitude(Double.parseDouble(apiResult.boundingbox.get(2)));
                result.setBoundaryMaxLatitude(Double.parseDouble(apiResult.boundingbox.get(1)));
                result.setBoundaryMaxLongitude(Double.parseDouble(apiResult.boundingbox.get(3)));
                PlaceInformation information = new PlaceInformation();
                information.setFormattedAddress(apiResult.display_name);
                information.setLanguageCode(language);
                information.setLongName(apiResult.name);
                information.setShortName(apiResult.name);
                information.setPlace(result);
                result.getInformation().add(information);
                return Optional.of(result);
            } else {
                return Optional.empty();
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to retrieve " + request.url(), ex);
        }
    }

    private Request createRequest(double latitude, double longitude, int zoom, String language) {
        HttpUrl url = baseUrl.newBuilder() //
            .addPathSegment("reverse") //
            .addQueryParameter("format", "jsonv2") //
            .addQueryParameter("lat", String.valueOf(latitude)) //
            .addQueryParameter("lon", String.valueOf(longitude)) //
            .addQueryParameter("zoom", String.valueOf(zoom)) //
            .addQueryParameter("accept-language", language) //
            .build();
        return new Request.Builder().url(url).build();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class NominatimResult {
        public long place_id;

        public String addresstype;

        public String name;

        public String display_name;

        public List<String> boundingbox;
    }
}
