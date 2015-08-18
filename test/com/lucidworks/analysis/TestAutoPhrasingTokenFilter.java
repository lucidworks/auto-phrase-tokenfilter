package com.lucidworks.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.util.CharArraySet;

import java.util.Arrays;

public class TestAutoPhrasingTokenFilter extends BaseTokenStreamTestCase {

    private static CharArraySet getPhraseSets(String... phrases) {
        return new CharArraySet(TEST_VERSION_CURRENT, Arrays.asList(phrases), false);
    }

    public void testNoPhrasesNoReplaceNoInput() throws Exception {
        final CharArraySet phrases = getPhraseSets();
        final String input = "";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases);
        assertAnalyzesTo(analyzer, input,
                new String[] {});
    }

    public void testNoPhrasesNoReplaceOneCharInput() throws Exception {
        final CharArraySet phrases = getPhraseSets();
        final String input = "A";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases);
        assertAnalyzesTo(analyzer, input,
                new String[] {"A"},
                new int[] {0},
                new int[] {1},
                new int[] {1});
    }

    public void testNoPhrasesNoReplaceOneWordInput() throws Exception {
        final CharArraySet phrases = getPhraseSets();
        final String input = "word";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases);
        assertAnalyzesTo(analyzer, input,
                new String[] {"word"},
                new int[] {0},
                new int[] {4},
                new int[] {1});
    }

    public void testNoPhrasesNoReplaceTwoCharsInput() throws Exception {
        final CharArraySet phrases = getPhraseSets();
        final String input = "A B";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases);
        assertAnalyzesTo(analyzer, input,
                new String[] {"A", "B"},
                new int[] {0, 2},
                new int[] {1, 3},
                new int[] {1, 1});
    }

    public void testNoPhrasesNoReplaceTwoWordsInput() throws Exception {
        final CharArraySet phrases = getPhraseSets();
        final String input = "two words";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases);
        assertAnalyzesTo(analyzer, input,
                new String[] {"two", "words"},
                new int[] {0, 4},
                new int[] {3, 9},
                new int[] {1, 1});
    }

    public void testNoPhrasesWithReplaceNoInput() throws Exception {
        final CharArraySet phrases = getPhraseSets();
        final String input = "";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, '_');
        assertAnalyzesTo(analyzer, input,
                new String[] {});
    }

    public void testNoPhrasesWithReplaceOneCharInput() throws Exception {
        final CharArraySet phrases = getPhraseSets();
        final String input = "A";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, '_');
        assertAnalyzesTo(analyzer, input,
                new String[] {"A"},
                new int[] {0},
                new int[] {1},
                new int[] {1});
    }

    public void testNoPhrasesWithReplaceOneWordInput() throws Exception {
        final CharArraySet phrases = getPhraseSets();
        final String input = "word";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, '_');
        assertAnalyzesTo(analyzer, input,
                new String[] {"word"},
                new int[] {0},
                new int[] {4},
                new int[] {1});
    }

    public void testNoPhrasesWithReplaceTwoCharsInput() throws Exception {
        final CharArraySet phrases = getPhraseSets();
        final String input = "A B";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, '_');
        assertAnalyzesTo(analyzer, input,
                new String[] {"A", "B"},
                new int[] {0, 2},
                new int[] {1, 3},
                new int[] {1, 1});
    }

    public void testNoPhrasesWithReplaceTwoWordsInput() throws Exception {
        final CharArraySet phrases = getPhraseSets();
        final String input = "two words";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, '_');
        assertAnalyzesTo(analyzer, input,
                new String[] {"two", "words"},
                new int[] {0, 4},
                new int[] {3, 9},
                new int[] {1, 1});
    }

    public void testOnePhraseNoReplaceNoInput() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair");
        final String input = "";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases);
        assertAnalyzesTo(analyzer, input,
                new String[] {});
    }

    public void testOnePhraseNoReplaceOneCharInput() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair");
        final String input = "A";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases);
        assertAnalyzesTo(analyzer, input,
                new String[] {"A"},
                new int[] {0},
                new int[] {1},
                new int[] {1});
    }

    public void testOnePhraseNoReplaceOneWordInput() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair");
        final String input = "word";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases);
        assertAnalyzesTo(analyzer, input,
                new String[] {"word"},
                new int[] {0},
                new int[] {4},
                new int[] {1});
    }

    public void testOnePhraseNoReplaceTwoCharsInput() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair");
        final String input = "A B";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases);
        assertAnalyzesTo(analyzer, input,
                new String[] {"A", "B"},
                new int[] {0, 2},
                new int[] {1, 3},
                new int[] {1, 1});
    }

    public void testOnePhraseNoReplaceTwoWordsInput() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair");
        final String input = "two words";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases);
        assertAnalyzesTo(analyzer, input,
                new String[] {"two", "words"},
                new int[] {0, 4},
                new int[] {3, 9},
                new int[] {1, 1});
    }

    public void testOnePhraseWithReplaceNoInput() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair");
        final String input = "";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, '_');
        assertAnalyzesTo(analyzer, input,
                new String[] {});
    }

    public void testOnePhraseWithReplaceOneCharInput() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair");
        final String input = "A";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, '_');
        assertAnalyzesTo(analyzer, input,
                new String[] {"A"},
                new int[] {0},
                new int[] {1},
                new int[] {1});
    }

    public void testOnePhraseWithReplaceOneWordInput() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair");
        final String input = "word";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, '_');
        assertAnalyzesTo(analyzer, input,
                new String[] {"word"},
                new int[] {0},
                new int[] {4},
                new int[] {1});
    }

    public void testOnePhraseWithReplaceTwoCharsInput() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair");
        final String input = "A B";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, '_');
        assertAnalyzesTo(analyzer, input,
                new String[] {"A", "B"},
                new int[] {0, 2},
                new int[] {1, 3},
                new int[] {1, 1});
    }

    public void testOnePhraseWithReplaceTwoWordsInput() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair");
        final String input = "two words";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, '_');
        assertAnalyzesTo(analyzer, input,
                new String[] {"two", "words"},
                new int[] {0, 4},
                new int[] {3, 9},
                new int[] {1, 1});
    }

    public void testOnePhraseNoReplacePartialPhraseInputStart() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair");
        final String input = "wheel";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases);
        assertAnalyzesTo(analyzer, input,
                new String[] {"wheel"},
                new int[] {0},
                new int[] {5},
                new int[] {1});
    }

    public void testOnePhraseNoReplacePartialPhraseInputEnd() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair");
        final String input = "chair";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases);
        assertAnalyzesTo(analyzer, input,
                new String[] {"chair"},
                new int[] {0},
                new int[] {5},
                new int[] {1});
    }

    public void testOnePhraseNoReplacePhraseMatch() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair");
        final String input = "wheel chair";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases);
        assertAnalyzesTo(analyzer, input,
                new String[] {"wheelchair"},
                new int[] {0},
                new int[] {10},
                new int[] {1});
    }

    public void testOnePhraseWithReplacePartialPhraseInputStart() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair");
        final String input = "wheel";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, '_');
        assertAnalyzesTo(analyzer, input,
                new String[] {"wheel"},
                new int[] {0},
                new int[] {5},
                new int[] {1});
    }

    public void testOnePhraseWithReplacePartialPhraseInputEnd() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair");
        final String input = "chair";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases, '_');
        assertAnalyzesTo(analyzer, input,
                new String[] {"chair"},
                new int[] {0},
                new int[] {5},
                new int[] {1});
    }

    public void testPhraseWithPrecedingNonPhraseWord() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair");
        final String input = "some wheel chair";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases);
        assertAnalyzesTo(analyzer, input,
                new String[] {"some", "wheelchair"},
                new int[] {0, 5},
                new int[] {4, 15},
                new int[] {1, 1});
    }

    public void testPhraseWithFollowingNonPhraseWord() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair");
        final String input = "wheel chair sauce";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases);
        assertAnalyzesTo(analyzer, input,
                new String[] {"wheelchair", "sauce"},
                new int[] {0, 11},
                new int[] {10, 16},
                new int[] {1, 1});
    }

    public void testTwoPhrases() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair", "foo bar");
        final String input = "wheel chair foo bar";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases);
        assertAnalyzesTo(analyzer, input,
                new String[] {"wheelchair", "foobar"},
                new int[] {0, 11},
                new int[] {10, 17},
                new int[] {1, 1});
    }

    public void testTwoPhrasesSomethingInBetween() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair", "foo bar");
        final String input = "wheel chair hello foo bar";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases);
        assertAnalyzesTo(analyzer, input,
                new String[] {"wheelchair", "hello", "foobar"},
                new int[] {0, 11, 17},
                new int[] {10, 16, 23},
                new int[] {1, 1, 1});
    }

    public void testTwoPhrasesTwoWordsInBetween() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair", "foo bar");
        final String input = "wheel chair hello there foo bar";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases);
        assertAnalyzesTo(analyzer, input,
                new String[] {"wheelchair", "hello", "there", "foobar"},
                new int[] {0, 11, 17, 23},
                new int[] {10, 16, 22, 29},
                new int[] {1, 1, 1, 1});
    }

    public void testTwoPhrasesPrecedingWord() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair", "foo bar");
        final String input = "hello wheel chair foo bar";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases);
        assertAnalyzesTo(analyzer, input,
                new String[] {"hello", "wheelchair", "foobar"},
                new int[] {0, 6, 17},
                new int[] {5, 16, 23},
                new int[] {1, 1, 1});
    }

    public void testTwoCompetingPhrases() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair", "chair alarm");
        final String input = "wheel chair alarm";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases);
        assertAnalyzesTo(analyzer, input,
                new String[] {"wheelchair", "alarm"},
                new int[] {0, 11},
                new int[] {10, 16},
                new int[] {1, 1});
    }

    public void testTwoCompetingPhrasesBothPhrases() throws Exception {
        final CharArraySet phrases = getPhraseSets("wheel chair", "chair alarm");
        final String input = "wheel chair chair alarm";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases);
        assertAnalyzesTo(analyzer, input,
                new String[] {"wheelchair", "chairalarm"},
                new int[] {0, 11},
                new int[] {10, 21},
                new int[] {1, 1});
    }

    public void testMultiplePhraseMatchUsesLongest() throws Exception {
        final CharArraySet phrases = getPhraseSets("corn bread", "corn bread dressing");
        final String input = "corn bread dressing";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases);
        assertAnalyzesTo(analyzer, input,
                new String[] {"cornbreaddressing"},
                new int[] {0},
                new int[] {17},
                new int[] {1});
    }

    public void testFuzzyMatchingMultipleTokens() throws Exception {
        final CharArraySet phrases = getPhraseSets("corn TOKEN TOKEN bread");
        final String input = "corn on my bread";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases);
        assertAnalyzesTo(analyzer, input,
                new String[] {"cornbread"},
                new int[] {0},
                new int[] {9},
                new int[] {1});
    }

    public void testFuzzyMatchingSingleToken() throws Exception {
        final CharArraySet phrases = getPhraseSets("corn TOKEN bread");
        final String input = "corn or bread";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases);
        assertAnalyzesTo(analyzer, input,
                new String[] {"cornbread"},
                new int[] {0},
                new int[] {9},
                new int[] {1});
    }

    public void testFuzzyMatchingTokenOptional() throws Exception {
        final CharArraySet phrases = getPhraseSets("corn TOKEN bread");
        final String input = "corn bread";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases);
        assertAnalyzesTo(analyzer, input,
                new String[] {"cornbread"},
                new int[] {0},
                new int[] {9},
                new int[] {1});
    }

    public void testFuzzyMatchingTokenWithMultipleWordsAfter() throws Exception {
        final CharArraySet phrases = getPhraseSets("first TOKEN second third");
        final String input = "first or second third";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases);
        assertAnalyzesTo(analyzer, input,
                new String[] {"firstsecondthird"},
                new int[] {0},
                new int[] {16},
                new int[] {1});
    }

    public void testFuzzyMatchingOptionalTokenWithMultipleWordsAfter() throws Exception {
        final CharArraySet phrases = getPhraseSets("first TOKEN second third");
        final String input = "first second third";

        Analyzer analyzer = new AutoPhrasingAnalyzer(phrases);
        assertAnalyzesTo(analyzer, input,
                new String[] {"firstsecondthird"},
                new int[] {0},
                new int[] {16},
                new int[] {1});
    }

}
