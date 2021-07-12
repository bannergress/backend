package com.bannergress.backend.restrictedarea.overpass;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

interface OverpassApi {
    @GetExchange(url = "https://overpass-api.de/api/interpreter")
    OverpassApiResult query(@RequestParam String data);
}
