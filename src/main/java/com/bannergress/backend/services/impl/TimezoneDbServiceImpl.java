package com.bannergress.backend.services.impl;

import com.bannergress.backend.services.TimezoneService;
import com.bannergress.backend.utils.Spatial;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.util.concurrent.RateLimiter;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.ZoneId;

/**
 * Service for resolving time zones using timezonedb.com.
 */
@Service
@Profile("timezonedb")
public class TimezoneDbServiceImpl implements TimezoneService {
    private final String apiKey;

    private final WebClient client;

    private final RateLimiter rateLimiter;

    @Autowired
    public TimezoneDbServiceImpl(@Value("${timezonedb.api-key}") String apiKey,
        @Value("${timezonedb.base-url:https://api.timezonedb.com}") String baseUrl,
        @Value("${timezonedb.requests-per-second:1}") double requestsPerSecond) {
        this.apiKey = apiKey;
        this.client = WebClient.create(baseUrl);
        this.rateLimiter = RateLimiter.create(requestsPerSecond);
    }

    @Override
    public ZoneId getZone(Point point) {
        checkRateLimit();
        ApiResponse response = callApi(Spatial.getLatitude(point), Spatial.getLongitude(point));
        switch (response.status) {
            case OK:
                return ZoneId.of(response.zoneName);
            case FAILED:
                throw new RuntimeException(response.message);
            default:
                throw new IllegalArgumentException(response.status.toString());
        }
    }

    /**
     * Calls the API with coordinates.
     *
     * @param latitude  Latitude to resolve.
     * @param longitude Longitude to resolve.
     * @return API response.
     */
    private ApiResponse callApi(double latitude, double longitude) {
        return client //
            .get() //
            .uri(builder -> {
                return builder //
                    .path("/v2.1/get-time-zone")//
                    .queryParam("key", apiKey) //
                    .queryParam("format", "json") //
                    .queryParam("fields", "zoneName") //
                    .queryParam("by", "position") //
                    .queryParam("lat", latitude) //
                    .queryParam("lng", longitude) //
                    .build();
            }) //
            .retrieve() //
            .bodyToMono(ApiResponse.class) //
            .block();
    }

    /** Rate limits requests, aborts if there are too many queued requests. */
    private void checkRateLimit() {
        if (!rateLimiter.tryAcquire(Duration.ofSeconds(30))) {
            throw new IllegalStateException("Rate limit exceeeded.");
        }
    }

    /** API response. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ApiResponse {
        ApiResponseStatus status;

        String message;

        String zoneName;
    }

    /** API response status. */
    private enum ApiResponseStatus {
        OK, FAILED
    }
}
