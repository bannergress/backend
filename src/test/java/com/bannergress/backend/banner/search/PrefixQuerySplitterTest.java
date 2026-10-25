package com.bannergress.backend.banner.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PrefixQuerySplitterTest {
    private final Analyzer analyzer = new StandardAnalyzer();

    private String split(String query) {
        return PrefixQuerySplitter.split(query, analyzer, "title");
    }

    @Test
    void splitsChineseAndJapanesePrefixWords() {
        assertThat(split("上理工*")).isEqualTo("上 理 工*");
        assertThat(split("へいく*")).isEqualTo("へ い く*");
        assertThat(split("東京タワ*")).isEqualTo("東 京 タワ*");
        assertThat(split("Tokyo 東京*")).isEqualTo("Tokyo 東 京*");
    }

    @Test
    void leavesWordsTheIndexKeepsWholeAlone() {
        assertThat(split("Amst*")).isEqualTo("Amst*");
        assertThat(split("Züri*")).isEqualTo("Züri*");
        assertThat(split("서울*")).isEqualTo("서울*");
        assertThat(split("トウキ*")).isEqualTo("トウキ*");
        assertThat(split("東*")).isEqualTo("東*");
    }

    @Test
    void leavesQueriesWithoutPrefixWordsAndOperatorsAlone() {
        assertThat(split("上理工")).isEqualTo("上理工");
        assertThat(split("\"上理工\"")).isEqualTo("\"上理工\"");
        assertThat(split("-上理工*")).isEqualTo("-上理工*");
        assertThat(split("")).isEqualTo("");
    }
}
