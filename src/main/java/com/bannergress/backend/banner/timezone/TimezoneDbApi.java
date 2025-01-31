package com.bannergress.backend.banner.timezone;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

public interface TimezoneDbApi {
    @GetExchange("v2.1/get-time-zone")
    TimezoneDbApiResponse getTimezone(@RequestParam String key, @RequestParam String format,
                                      @RequestParam String fields, @RequestParam String by,
                                      @RequestParam double latitude, @RequestParam double longitude);
}
