package com.bannergress.backend.restrictedarea;

import java.util.List;

public interface RestrictedAreaSuggestionService {
    List<RestrictedAreaSuggestion> getSuggestions(double latitude, double longitude);

    RestrictedArea getTemplate(String id);
}
