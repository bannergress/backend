package com.bannergress.backend.banner.timezone;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

interface TimezoneDbApi {
    @GetExchange("/v2.1/get-time-zone?format=json&fields=zoneName&by=position")
    Response getTimeZone(@RequestParam String key, @RequestParam double lat, @RequestParam double lng);

    /** API response. */
    class Response {
        public ResponseStatus status;

        public String message;

        public String zoneName;
    }

    /** API response status. */
    enum ResponseStatus {
        OK, FAILED
    }
}
