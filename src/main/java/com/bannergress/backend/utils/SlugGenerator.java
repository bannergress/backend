package com.bannergress.backend.utils;

import org.springframework.security.crypto.codec.Hex;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Generator for slugs (readable lowercase URL component).
 */
public final class SlugGenerator {
    private final int suffixBytes;

    private static final Pattern replacedCharacters = Pattern.compile("[^\\p{Alnum}]+",
        Pattern.UNICODE_CHARACTER_CLASS);

    /**
     * Creates a new slug generator.
     *
     * @param suffixBytes Number of bytes to use for the suffix. Suffix is represented in hexadecimal notation.
     */
    public SlugGenerator(int suffixBytes) {
        this.suffixBytes = suffixBytes;
    }

    private static final SecureRandom numberGenerator = new SecureRandom();

    /**
     * Generates a slug (readable lowercase URL component) out of a base string.
     *
     * @param base        Base string.
     * @param isAvailable Predicate that checks for collisions of the slug.
     * @return Slug.
     */
    public String generateSlug(String base, Predicate<String> isAvailable) {
        String prefix = getPrefix(base);
        while (true) {
            byte[] random = new byte[suffixBytes];
            numberGenerator.nextBytes(random);
            String proposal = prefix + "-" + new String(Hex.encode(random));
            if (isAvailable.test(proposal)) {
                return proposal;
            }
        }
    }

    private String getPrefix(String base) {
        String lowerCase = base.toLowerCase(Locale.ROOT);
        String onlyAlphanum = replacedCharacters.matcher(lowerCase).replaceAll("-");
        String prefix = onlyAlphanum.replaceAll("^-+|-+$", "");
        return prefix;
    }

    /**
     * Checks whether a slug is derived from a base string.
     *
     * @param slug Slug.
     * @param base Base string.
     * @return <code>true</code> whether the slug is derived from the base string.
     */
    public boolean isDerivedFrom(String slug, String base) {
        String prefix = getPrefix(base);
        if (slug.startsWith(prefix)) {
            String remainder = slug.substring(prefix.length());
            return remainder.matches("^-[0-9a-f]+$");
        } else {
            return false;
        }
    }
}
