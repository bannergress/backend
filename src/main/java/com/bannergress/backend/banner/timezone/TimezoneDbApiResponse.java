package com.bannergress.backend.banner.timezone;

import com.bannergress.backend.banner.timezone.TimezoneDbServiceImpl.ApiResponseStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** API response. */
@JsonIgnoreProperties(ignoreUnknown = true)
class TimezoneDbApiResponse {
    public ApiResponseStatus status;

    public String message;

    public String zoneName;
}
