package com.lucidworks.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.util.CharArraySet;

import java.util.Arrays;

public class TestAutoPhrasingTokenFilter extends BaseTokenStreamTestCase {

    private static CharArraySet getPhraseSets(String... phrases) {
        return new CharArraySet(TEST_VERSION_CURRENT, Arrays.asList(phrases), false);
    }

    public void testOneWord() throws Exception {
        final CharArraySet phrases = getPhraseSets("income tax", "tax refund", "property tax");
        final String input = "word";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, '_');
        assertAnalyzesTo(analyzer, input,
                new String[] {"word"},
                new int[] {0},
                new int[] {4},
                new int[] {1});
    }

    public void testAutoPhrase() throws Exception {
        final CharArraySet phrases = getPhraseSets("income tax", "tax refund", "property tax");
        final String input = "what is my income tax refund this year now that my property tax is so high";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, '_');
        assertAnalyzesTo(analyzer, input,
                new String[] {"what", "is", "my", "income_tax", "tax_refund", "this", "year", "now", "that", "my",
                        "property_tax", "is", "so", "high"},
                new int[] {0, 5, 8, 11, 18, 29, 34, 39, 43, 48, 51, 64, 67, 70}, // this is wrong, so it goes
                new int[] {4, 7, 10, 21, 28, 33, 38, 42, 47, 50, 63, 66, 69, 74});
    }

    public void testOverlappingAtBeginning() throws Exception {
        final CharArraySet phrases = getPhraseSets("new york", "new york city", "city of new york");
        final String input = "new york city is great";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, '_');
        assertAnalyzesTo(analyzer, input,
                new String[] {"new_york_city", "is", "great"});
    }

    public void testOverlappingAtEnd() throws Exception {
        final CharArraySet phrases = getPhraseSets("new york", "new york city", "city of new york");
        final String input = "the great city of new york";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, '_');
        assertAnalyzesTo(analyzer, input,
                new String[]{"the", "great", "city_of_new_york"});
    }

    public void testIncompletePhrase() throws Exception {
        final CharArraySet phrases = getPhraseSets("big apple", "new york city", "property tax", "three word phrase");
        final String input = "some new york city";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, '_');
        assertAnalyzesTo(analyzer, input,
                new String[]{"some", "new_york_city"});
    }

    public void testPhrasesNullReplace() throws Exception {
        final CharArraySet phrases = getPhraseSets("big apple", "new york city", "property tax", "three word phrase");
        final String input = "some new york city something";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases);
        assertAnalyzesTo(analyzer, input,
                new String[]{"some", "newyorkcity", "something"});
    }

    public void testPhrasesNullReplacePartialPhraseMatchIsOnlyToken() throws Exception {
        final CharArraySet phrases = getPhraseSets("big apple", "new york city", "property tax", "three word phrase");
        final String input = "big";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases);
        assertAnalyzesTo(analyzer, input,
                new String[]{"big"});
    }

    public void testPhrasesNullReplacePartialPhraseMatch() throws Exception {
        final CharArraySet phrases = getPhraseSets("big apple", "new york city", "property tax", "three word phrase");
        final String input = "big orange";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases);
        assertAnalyzesTo(analyzer, input,
                new String[]{"big", "orange"});
    }

    public void testPhrasesNullReplacePartialPhraseMatchPartOnEnd() throws Exception {
        final CharArraySet phrases = getPhraseSets("big apple", "new york city", "property tax", "three word phrase");
        final String input = "orange big";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases);
        assertAnalyzesTo(analyzer, input,
                new String[]{"orange", "big"});
    }

    public void testPhrasesNullReplacePartialPhraseMatchPrecedingStuff() throws Exception {
        final CharArraySet phrases = getPhraseSets("big apple", "new york city", "property tax", "three word phrase");
        final String input = "something big orange";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases);
        assertAnalyzesTo(analyzer, input,
                new String[]{"something", "big", "orange"});
    }

    public void testPhrasesNullReplacePartialPhraseMatchPartOnEndPrecedingStuff() throws Exception {
        final CharArraySet phrases = getPhraseSets("big apple", "new york city", "property tax", "three word phrase");
        final String input = "new york city something orange big";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases);
        assertAnalyzesTo(analyzer, input,
                new String[]{"newyorkcity", "something", "orange", "big"});
    }
}
