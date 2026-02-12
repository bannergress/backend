package com.bannergress.backend.banner.timezone;

import com.bannergress.backend.banner.timezone.TimezoneDbApi.Response;
import com.bannergress.backend.spatial.Spatial;
import com.google.common.util.concurrent.RateLimiter;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.service.registry.ImportHttpServices;

import java.time.Duration;
import java.time.ZoneId;

/**
 * Service for resolving time zones using timezonedb.com.
 */
@Service
@ImportHttpServices(group = "timezonedb", types = TimezoneDbApi.class)
class TimezoneDbServiceImpl implements TimezoneService {
    private final String apiKey;

    private final TimezoneDbApi timezoneDbApi;

    private final RateLimiter rateLimiter;

    public TimezoneDbServiceImpl(@Value("${timezonedb.api-key}") String apiKey, TimezoneDbApi timezoneDbApi,
        @Value("${timezonedb.requests-per-second:1}") double requestsPerSecond) {
        this.apiKey = apiKey;
        this.timezoneDbApi = timezoneDbApi;
        this.rateLimiter = RateLimiter.create(requestsPerSecond);
    }

    @Override
    public ZoneId getZone(Point point) {
        checkRateLimit();
        Response response = timezoneDbApi.getTimeZone(apiKey, Spatial.getLatitude(point), Spatial.getLongitude(point));
        switch (response.status) {
            case OK:
                return ZoneId.of(response.zoneName);
            case FAILED:
                throw new RuntimeException(response.message);
            default:
                throw new IllegalArgumentException(response.status.toString());
        }
    }

    /** Rate limits requests, aborts if there are too many queued requests. */
    private void checkRateLimit() {
        if (!rateLimiter.tryAcquire(Duration.ofSeconds(30))) {
            throw new IllegalStateException("Rate limit exceeeded.");
        }
    }
}
