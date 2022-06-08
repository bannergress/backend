package com.bannergress.backend.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;

/**
 * Resolves arguments of type List<Locale.LanguageRange> as a priority list of languages.
 */
public class LanguageRangeResolver implements HandlerMethodArgumentResolver {
    @SuppressWarnings("serial")
    private static final TypeToken<List<Locale.LanguageRange>> LANGUAGE_RANGE_LIST_TYPE = new TypeToken<List<Locale.LanguageRange>>() {
    };

    @Override
    public List<Locale.LanguageRange> resolveArgument(MethodParameter parameter, ModelAndViewContainer container,
                                                      NativeWebRequest request, WebDataBinderFactory binderFactory) {
        String header = request.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
        if (header == null) {
            return ImmutableList.of();
        }
        try {
            return ImmutableList.copyOf(Locale.LanguageRange.parse(header));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Type parameterizedType = parameter.getParameter().getParameterizedType();
        return LANGUAGE_RANGE_LIST_TYPE.isSupertypeOf(parameterizedType);
    }
}
