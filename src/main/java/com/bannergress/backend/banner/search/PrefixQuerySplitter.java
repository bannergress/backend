package com.bannergress.backend.banner.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Splits prefix words ({@code word*}) of a simple query string the way the index splits text.
 *
 * <p>
 * Prefix queries are not analyzed, only normalized. The standard tokenizer indexes Chinese characters and Japanese
 * hiragana as one token per character, so {@code 上理工*} looks for a token starting with {@code 上理工} and finds
 * nothing, while {@code 上理工} finds the banner. Splitting the word with the analyzer first turns it into
 * {@code 上 理 工*}: the leading tokens must all match and only the last one is a prefix. Words the analyzer keeps
 * whole (Latin, Korean, katakana, Thai) come out unchanged.
 */
final class PrefixQuerySplitter {
    /** A word ending in {@code *}, without simple query string operators in it. */
    private static final Pattern PREFIX_WORD = Pattern.compile("(?<=^|\\s)([^\\s\"+|()~*-]+)\\*(?=\\s|$)");

    private PrefixQuerySplitter() {
    }

    static String split(String query, Analyzer analyzer, String field) {
        Matcher matcher = PREFIX_WORD.matcher(query);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            List<String> tokens = tokens(analyzer, field, matcher.group(1));
            String replacement = tokens.size() > 1 ? String.join(" ", tokens) + "*" : matcher.group(0);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        return matcher.appendTail(result).toString();
    }

    private static List<String> tokens(Analyzer analyzer, String field, String text) {
        List<String> tokens = new ArrayList<>();
        try (TokenStream stream = analyzer.tokenStream(field, text)) {
            CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);
            stream.reset();
            while (stream.incrementToken()) {
                tokens.add(term.toString());
            }
            stream.end();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return tokens;
    }
}
