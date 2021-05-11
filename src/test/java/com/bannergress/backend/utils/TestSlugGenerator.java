package com.bannergress.backend.utils;

import com.google.common.base.Predicates;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TestSlugGenerator {
    @Test
    void testReplacements() {
        SlugGenerator generator = new SlugGenerator(2);

        String actual1 = generator.generateSlug("[Augsburg ist schön seit 15 v. Chr.]", Predicates.alwaysTrue());
        assertThat(actual1).matches("^augsburg-ist-schön-seit-15-v-chr-[0-9a-f]{4}$");

        String actual2 = generator.generateSlug("Волковское кладбище", Predicates.alwaysTrue());
        assertThat(actual2).matches("^волковское-кладбище-[0-9a-f]{4}$");
    }

    @Test
    void testConflict() {
        SlugGenerator generator = new SlugGenerator(2);

        Set<String> tries = new HashSet<>();
        // Generate conflicts with all but one two-byte-suffixes
        String actual = generator.generateSlug("test", slug -> {
            if (tries.size() < 65_535) {
                tries.add(slug);
                return false;
            } else {
                return !tries.contains(slug);
            }
        });
        assertThat(tries.size()).isEqualTo(65_535);
        assertThat(actual).isNotIn(tries);
    }
}