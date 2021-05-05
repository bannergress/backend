package com.bannergress.backend.utils;

import org.springframework.security.crypto.codec.Hex;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class SlugGenerator {
    private static final int SUFFIX_BYTES = 2;

    private static final Pattern replacedCharacters = Pattern.compile("[^\\p{Alnum}]+",
        Pattern.UNICODE_CHARACTER_CLASS);

    private static final SecureRandom numberGenerator = new SecureRandom();

    /**
     * Generates a slug (readable lowercase URL component) out of a base string.
     *
     * @param base        Base string.
     * @param isAvailable Predicate that checks for collisions of the slug.
     * @return Slug.
     */
    public static String generateSlug(String base, Predicate<String> isAvailable) {
        String lowerCase = base.toLowerCase(Locale.ROOT);
        String onlyAlphanum = replacedCharacters.matcher(lowerCase).replaceAll("-");
        String prefix = onlyAlphanum.replaceAll("^-+|-+$", "");
        while (true) {
            byte[] random = new byte[SUFFIX_BYTES];
            numberGenerator.nextBytes(random);
            String proposal = prefix + "-" + new String(Hex.encode(random));
            if (isAvailable.test(proposal)) {
                return proposal;
            }
        }
    }

    private SlugGenerator() {
    }
}
