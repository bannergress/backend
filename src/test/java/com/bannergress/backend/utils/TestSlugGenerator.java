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

        String base1 = "[Augsburg ist schön seit 15 v. Chr.]";
        String actual1 = generator.generateSlug(base1, Predicates.alwaysTrue());
        assertThat(actual1).matches("^augsburg-ist-schön-seit-15-v-chr-[0-9a-f]{4}$");
        assertThat(generator.isDerivedFrom(actual1, base1));

        String base2 = "Волковское кладбище";
        String actual2 = generator.generateSlug(base2, Predicates.alwaysTrue());
        assertThat(actual2).matches("^волковское-кладбище-[0-9a-f]{4}$");
        assertThat(generator.isDerivedFrom(actual2, base2));
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
