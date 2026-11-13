package com.bannergress.backend.banner.search;

import java.util.List;
import java.util.UUID;

record ListTypeFilter(List<UUID> uuidsSorted, Method method) {
    enum Method {
        INCLUDE, EXCLUDE, IGNORE
    }
}
