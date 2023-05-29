package com.bannergress.backend.testutils.builder;

import com.bannergress.backend.utils.PojoBuilder;
import jakarta.validation.constraints.NotNull;

import java.util.*;

import static com.bannergress.backend.testutils.builder.JavatypeBuilder.random;

public class BuilderMethods {
    @NotNull
    public static <T> T a(final PojoBuilder<T> builder) {
        return builder.build();
    }

    @NotNull
    public static <T> List<T> listWith(final PojoBuilder<T> builder) {
        return List.of(builder.build());
    }

    @NotNull
    public static <T> Set<T> setWith(final PojoBuilder<T> builder) {
        return Set.of(builder.build());
    }

    @NotNull
    public static <T, K> SortedMap<T, K> sortedMapWith(final T key, final PojoBuilder<K> builder) {
        return new TreeMap<>(Map.of(key, builder.build()));
    }

    @SafeVarargs
    @NotNull
    public static <T> T oneOf(final T... args) {
        return args[random(0, args.length)];
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public static <T> T[] arrayWith(final PojoBuilder<T> builder) {
        return (T[]) new Object[]{builder.build()};
    }
}
