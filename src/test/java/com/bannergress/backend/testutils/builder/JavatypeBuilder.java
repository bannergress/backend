package com.bannergress.backend.testutils.builder;

import com.bannergress.backend.utils.PojoBuilder;
import jakarta.validation.constraints.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static com.bannergress.backend.testutils.builder.BuilderMethods.a;

public class JavatypeBuilder {

    private static final Integer START_VALUE_TO_AVOID_INTEGER_CACHING_WHICH_WORKS_WITH_DOUBLE_EQUALS = 128 + 1;
    private static final Random random = new Random();
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final AtomicInteger unique = new AtomicInteger(START_VALUE_TO_AVOID_INTEGER_CACHING_WHICH_WORKS_WITH_DOUBLE_EQUALS);
    
    @NotNull
    public static PojoBuilder<Integer> $Int() {
        return JavatypeBuilder::getUnique;
    }

    @NotNull
    public static PojoBuilder<Long> $Long() {
        return () -> (long) getUnique();
    }

    @NotNull
    public static PojoBuilder<Float> $Float() {
        return () -> ((float) a($Int())) / random(1, 10);
    }

    @NotNull
    public static PojoBuilder<Double> $Double() {
        return () -> ((double) a($Int())) / random(1, 10);
    }

    @NotNull
    public static PojoBuilder<Boolean> $Boolean() {
        return random::nextBoolean;
    }

    @NotNull
    public static PojoBuilder<String> $String() {
        return $String("String_");
    }

    @NotNull
    public static PojoBuilder<String> $String(final String prefix) {
        return () -> prefix + uniqueString();
    }

    @NotNull
    public static PojoBuilder<URL> $URL() {
        return () -> {
            try {
                return new URL(a($String("http://dummy/")));
            } catch (MalformedURLException e) {
                return null;
            }
        };
    }

    @NotNull
    public static PojoBuilder<Byte> $Byte() {
        return () -> (byte) getUnique();
    }

    @NotNull
    public static PojoBuilder<UUID> $UUID() {
        return UUID::randomUUID;
    }

    @NotNull
    public static int random(final int start, final int end) {
        return random.nextInt(end - start) + start;
    }

    @NotNull
    private static int getUnique() {
        return unique.getAndIncrement();
    }

    @NotNull
    private static String uniqueString() {
        final int unique = getUnique();
        final int index = unique % ALPHANUMERIC.length();
        return Integer.toHexString(unique)
            + ALPHANUMERIC.substring(index, index + 1).toLowerCase()
            + ALPHANUMERIC.substring(index, index + 1).toUpperCase();
    }

    @NotNull
    public static PojoBuilder<Instant> $Instant() {
        return () -> Instant.ofEpochMilli(random.nextLong());
    }
}
