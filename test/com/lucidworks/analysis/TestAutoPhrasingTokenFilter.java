package com.lucidworks.analysis;

import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;

import java.io.StringReader;
import java.util.Arrays;

public class TestAutoPhrasingTokenFilter extends BaseTokenStreamTestCase {

    public void testAutoPhrase() throws Exception {
        final CharArraySet phraseSets = new CharArraySet(TEST_VERSION_CURRENT, Arrays.asList(
                "income tax", "tax refund", "property tax"
        ), false);

        final String input = "what is my income tax refund this year now that my property tax is so high";
        WhitespaceTokenizer wt = new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader(input));
        AutoPhrasingTokenFilter autoPhrasingTokenFilter =
                new AutoPhrasingTokenFilter(TEST_VERSION_CURRENT, wt, phraseSets, false);
        autoPhrasingTokenFilter.setReplaceWhitespaceWith('_');
        CharTermAttribute term = autoPhrasingTokenFilter.addAttribute(CharTermAttribute.class);
        autoPhrasingTokenFilter.reset();

        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("what", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("is", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("my", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("income_tax", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("tax_refund", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("this", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("year", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("now", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("that", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("my", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("property_tax", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("is", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("so", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("high", term.toString());

        assertFalse(autoPhrasingTokenFilter.incrementToken());
    }

    public void testAutoPhraseEmitSingle() throws Exception {
        final CharArraySet phraseSets = new CharArraySet(TEST_VERSION_CURRENT, Arrays.asList(
                "income tax", "tax refund", "property tax"
        ), false);

        final String input = "what is my income tax refund this year now that my property tax is so high";
        WhitespaceTokenizer wt = new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader(input));
        AutoPhrasingTokenFilter autoPhrasingTokenFilter =
                new AutoPhrasingTokenFilter(TEST_VERSION_CURRENT, wt, phraseSets, true);
        autoPhrasingTokenFilter.setReplaceWhitespaceWith('_');
        CharTermAttribute term = autoPhrasingTokenFilter.addAttribute(CharTermAttribute.class);
        autoPhrasingTokenFilter.reset();

        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("what", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("is", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("my", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("income", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("income_tax", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("tax", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("tax_refund", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("refund", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("this", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("year", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("now", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("that", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("my", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("property", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("property_tax", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("tax", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("is", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("so", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("high", term.toString());

        assertFalse(autoPhrasingTokenFilter.incrementToken());
    }

    public void testOverlappingAtBeginning() throws Exception {
        final CharArraySet phraseSets = new CharArraySet(TEST_VERSION_CURRENT, Arrays.asList(
                "new york", "new york city", "city of new york"
        ), false);

        final String input = "new york city is great";
        WhitespaceTokenizer wt = new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader(input));
        AutoPhrasingTokenFilter autoPhrasingTokenFilter =
                new AutoPhrasingTokenFilter(TEST_VERSION_CURRENT, wt, phraseSets, false);
        autoPhrasingTokenFilter.setReplaceWhitespaceWith('_');
        CharTermAttribute term = autoPhrasingTokenFilter.addAttribute(CharTermAttribute.class);
        autoPhrasingTokenFilter.reset();

        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("new_york_city", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("is", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("great", term.toString());

        assertFalse(autoPhrasingTokenFilter.incrementToken());
    }

    public void testOverlappingAtBeginningEmitSingle() throws Exception {
        final CharArraySet phraseSets = new CharArraySet(TEST_VERSION_CURRENT, Arrays.asList(
                "new york", "new york city", "city of new york"
        ), false);


        final String input = "new york city is great";
        WhitespaceTokenizer wt = new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader(input));
        AutoPhrasingTokenFilter autoPhrasingTokenFilter =
                new AutoPhrasingTokenFilter(TEST_VERSION_CURRENT, wt, phraseSets, true);
        autoPhrasingTokenFilter.setReplaceWhitespaceWith('_');
        CharTermAttribute term = autoPhrasingTokenFilter.addAttribute(CharTermAttribute.class);
        autoPhrasingTokenFilter.reset();

        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("new", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("york", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("new_york", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("new_york_city", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("city", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("is", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("great", term.toString());

        assertFalse(autoPhrasingTokenFilter.incrementToken());
    }

    public void testOverlappingAtEndEmitSingle() throws Exception {
        final CharArraySet phraseSets = new CharArraySet(TEST_VERSION_CURRENT, Arrays.asList(
                "new york", "new york city", "city of new york"
        ), false);

        final String input = "the great city of new york";

        WhitespaceTokenizer wt = new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader(input));
        AutoPhrasingTokenFilter autoPhrasingTokenFilter =
                new AutoPhrasingTokenFilter(TEST_VERSION_CURRENT, wt, phraseSets, true);
        autoPhrasingTokenFilter.setReplaceWhitespaceWith('_');
        CharTermAttribute term = autoPhrasingTokenFilter.addAttribute(CharTermAttribute.class);
        autoPhrasingTokenFilter.reset();

        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("the", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("great", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("city", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("of", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("new", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("york", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("city_of_new_york", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("new_york", term.toString());

        assertFalse(autoPhrasingTokenFilter.incrementToken());
    }

    public void testOverlappingAtEnd() throws Exception {
        final CharArraySet phraseSets = new CharArraySet(TEST_VERSION_CURRENT, Arrays.asList(
                "new york", "new york city", "city of new york"
        ), false);

        final String input = "the great city of new york";

        WhitespaceTokenizer wt = new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader(input));
        AutoPhrasingTokenFilter autoPhrasingTokenFilter =
                new AutoPhrasingTokenFilter(TEST_VERSION_CURRENT, wt, phraseSets, false);
        autoPhrasingTokenFilter.setReplaceWhitespaceWith('_');
        CharTermAttribute term = autoPhrasingTokenFilter.addAttribute(CharTermAttribute.class);
        autoPhrasingTokenFilter.reset();

        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("the", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("great", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("city_of_new_york", term.toString());

        assertFalse(autoPhrasingTokenFilter.incrementToken());
    }

    public void testIncompletePhrase() throws Exception {
        final CharArraySet phraseSets = new CharArraySet(TEST_VERSION_CURRENT, Arrays.asList(
                "big apple", "new york city", "property tax", "three word phrase"
        ), false);

        final String input = "some new york city";

        WhitespaceTokenizer wt = new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader(input));
        AutoPhrasingTokenFilter autoPhrasingTokenFilter =
                new AutoPhrasingTokenFilter(TEST_VERSION_CURRENT, wt, phraseSets, false);
        autoPhrasingTokenFilter.setReplaceWhitespaceWith('_');
        CharTermAttribute term = autoPhrasingTokenFilter.addAttribute(CharTermAttribute.class);
        autoPhrasingTokenFilter.reset();

        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("some", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("new_york_city", term.toString());

        assertFalse(autoPhrasingTokenFilter.incrementToken());
    }

    public void testPhrasesNullReplace() throws Exception {
        final CharArraySet phraseSets = new CharArraySet(TEST_VERSION_CURRENT, Arrays.asList(
                "big apple", "new york city", "property tax", "three word phrase"
        ), false);

        final String input = "some new york city something";

        WhitespaceTokenizer wt = new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader(input));
        AutoPhrasingTokenFilter autoPhrasingTokenFilter =
                new AutoPhrasingTokenFilter(TEST_VERSION_CURRENT, wt, phraseSets, false);
        autoPhrasingTokenFilter.setReplaceWhitespaceWith(null);
        CharTermAttribute term = autoPhrasingTokenFilter.addAttribute(CharTermAttribute.class);
        autoPhrasingTokenFilter.reset();

        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("some", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("newyorkcity", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("something", term.toString());

        assertFalse(autoPhrasingTokenFilter.incrementToken());
    }

    public void testPhrasesNullReplacePartialPhraseMatchIsOnlyToken() throws Exception {
        final CharArraySet phraseSets = new CharArraySet(TEST_VERSION_CURRENT, Arrays.asList(
                "big apple", "new york city", "property tax", "three word phrase"
        ), false);

        final String input = "big";

        WhitespaceTokenizer wt = new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader(input));
        AutoPhrasingTokenFilter autoPhrasingTokenFilter =
                new AutoPhrasingTokenFilter(TEST_VERSION_CURRENT, wt, phraseSets, false);
        autoPhrasingTokenFilter.setReplaceWhitespaceWith(null);
        CharTermAttribute term = autoPhrasingTokenFilter.addAttribute(CharTermAttribute.class);
        autoPhrasingTokenFilter.reset();

        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("big", term.toString());

        assertFalse(autoPhrasingTokenFilter.incrementToken());
    }

    public void testPhrasesNullReplacePartialPhraseMatch() throws Exception {
        final CharArraySet phraseSets = new CharArraySet(TEST_VERSION_CURRENT, Arrays.asList(
                "big apple", "new york city", "property tax", "three word phrase"
        ), false);

        final String input = "big orange";

        WhitespaceTokenizer wt = new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader(input));
        AutoPhrasingTokenFilter autoPhrasingTokenFilter =
                new AutoPhrasingTokenFilter(TEST_VERSION_CURRENT, wt, phraseSets, false);
        autoPhrasingTokenFilter.setReplaceWhitespaceWith(null);
        CharTermAttribute term = autoPhrasingTokenFilter.addAttribute(CharTermAttribute.class);
        autoPhrasingTokenFilter.reset();

        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("big", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("orange", term.toString());

        assertFalse(autoPhrasingTokenFilter.incrementToken());
    }

    public void testPhrasesNullReplacePartialPhraseMatchPartOnEnd() throws Exception {
        final CharArraySet phraseSets = new CharArraySet(TEST_VERSION_CURRENT, Arrays.asList(
                "big apple", "new york city", "property tax", "three word phrase"
        ), false);

        final String input = "orange big";

        WhitespaceTokenizer wt = new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader(input));
        AutoPhrasingTokenFilter autoPhrasingTokenFilter =
                new AutoPhrasingTokenFilter(TEST_VERSION_CURRENT, wt, phraseSets, false);
        autoPhrasingTokenFilter.setReplaceWhitespaceWith(null);
        CharTermAttribute term = autoPhrasingTokenFilter.addAttribute(CharTermAttribute.class);
        autoPhrasingTokenFilter.reset();

        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("orange", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("big", term.toString());

        assertFalse(autoPhrasingTokenFilter.incrementToken());
    }

    /*public void testPhrasesNullReplacePartialPhraseMatchPrecedingStuff() throws Exception {
        final CharArraySet phraseSets = new CharArraySet(TEST_VERSION_CURRENT, Arrays.asList(
                "big apple", "new york city", "property tax", "three word phrase"
        ), false);

        final String input = "something big orange";

        WhitespaceTokenizer wt = new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader(input));
        AutoPhrasingTokenFilter autoPhrasingTokenFilter =
                new AutoPhrasingTokenFilter(TEST_VERSION_CURRENT, wt, phraseSets, false);
        autoPhrasingTokenFilter.setReplaceWhitespaceWith(null);
        CharTermAttribute term = autoPhrasingTokenFilter.addAttribute(CharTermAttribute.class);
        autoPhrasingTokenFilter.reset();

        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("something", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("big", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("orange", term.toString());

        assertFalse(autoPhrasingTokenFilter.incrementToken());
    }*/

    public void testPhrasesNullReplacePartialPhraseMatchPartOnEndPrecedingStuff() throws Exception {
        final CharArraySet phraseSets = new CharArraySet(TEST_VERSION_CURRENT, Arrays.asList(
                "big apple", "new york city", "property tax", "three word phrase"
        ), false);

        final String input = "new york city something orange big";

        WhitespaceTokenizer wt = new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader(input));
        AutoPhrasingTokenFilter autoPhrasingTokenFilter =
                new AutoPhrasingTokenFilter(TEST_VERSION_CURRENT, wt, phraseSets, false);
        autoPhrasingTokenFilter.setReplaceWhitespaceWith(null);
        CharTermAttribute term = autoPhrasingTokenFilter.addAttribute(CharTermAttribute.class);
        autoPhrasingTokenFilter.reset();

        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("newyorkcity", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("something", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("orange", term.toString());
        assertTrue(autoPhrasingTokenFilter.incrementToken());
        assertEquals("big", term.toString());

        assertFalse(autoPhrasingTokenFilter.incrementToken());
    }
}
