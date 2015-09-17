package com.lucidworks.analysis;

import junit.framework.TestCase;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.WordlistLoader;
import org.apache.lucene.util.Version;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParserPlugin;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for the AutoPhrasingQParserPlugin
 * Note: The use of PowerMock with java 1.7.0_65 will blow up with a "Error exists in the bytecode" type message
 * More info at: https://code.google.com/p/powermock/issues/detail?id=504
 * Workaround is to add the -noverify vm option to the test run configuration
 */
@SuppressWarnings("UnnecessaryLocalVariable")
@RunWith(PowerMockRunner.class)
@PrepareForTest({WordlistLoader.class, SolrCore.class})
public class TestAutoPhrasingQParserPlugin extends TestCase {

    private final boolean DefaultIgnoreCase = false;
    private final String DownstreamParser = "edismax";
    private final Character DefaultReplaceWhitespaceWith = 'Z';
    private final Character EmptyReplaceWhitespaceWith = null;

    public void testCreateParserNoChangeSingleTerm() throws Exception {
        String actual = "something";
        String expected = "something";
        invokeCreateParser(actual, expected);
    }

    public void testCreateParserNoChangeMultipleTerms() throws Exception {
        String actual = "two things";
        String expected = "two things";
        invokeCreateParser(actual, expected);
    }

    public void testCreateParserEmptyQuery() throws Exception {
        String actual = "";
        String expected = "";
        invokeCreateParser(actual, expected);
    }

    public void testCreateParserOnlySpace() throws Exception {
        String actual = " ";
        String expected = "";
        invokeCreateParser(actual, expected);
    }

    public void testCreateParserFieldAndValue() throws Exception {
        String actual = "Field:Value";
        String expected = "Field:Value";
        invokeCreateParser(actual, expected);
    }

    public void testCreateParserMultipleThings() throws Exception {
        String actual = "Field:Value something else";
        String expected = "Field:Value something else";
        invokeCreateParser(actual, expected);
    }

    public void testCreateParserSimpleReplace() throws Exception {
        String actual = "wheel chair";
        String expected = String.format("wheel%cchair", DefaultReplaceWhitespaceWith);
        invokeCreateParser(actual, expected);
    }

    public void testCreateParserDoNotIgnoreCase() throws Exception {
        String actual = "Wheel Chair";
        String expected = "Wheel Chair";
        invokeCreateParser(actual, expected);
    }

    public void testCreateParserIgnoreCase() throws Exception {
        String actual = "Wheel Chair";
        String expected = String.format("wheel%cchair", DefaultReplaceWhitespaceWith);
        invokeCreateParser(actual, expected, true, DefaultReplaceWhitespaceWith);
    }

    public void testCreateParserMultiplePhrases() throws Exception {
        String actual = "wheel chair hi there";
        String expected = String.format("wheel%cchair hi%cthere", DefaultReplaceWhitespaceWith, DefaultReplaceWhitespaceWith);
        invokeCreateParser(actual, expected);
    }

    public void testCreateParserStemming() throws Exception {
        // Note: This is undesirable. Ideally, stemming would find it and fix it but it hasn't run yet.
        String actual = "wheel chairs";
        String expected = actual;
        invokeCreateParser(actual, expected);
    }

    public void testCreateParserPhraseQuery() throws Exception {
        String actual = "\"some phrase\"";
        String expected = actual;
        invokeCreateParser(actual, expected);
    }

    public void testCreateParserPhraseQueryWithAutoPhrase() throws Exception {
        String actual = "\"wheel chair\"";
        String expected = String.format("\"wheel%cchair\"", DefaultReplaceWhitespaceWith);
        invokeCreateParser(actual, expected);
    }

    public void testCreateParserMultiplePhraseQueries() throws Exception {
        String actual = "something \"wheel chair\" \"hi there\" something else";
        String expected = String.format("something \"wheel%cchair\" \"hi%cthere\" something else",
                DefaultReplaceWhitespaceWith, DefaultReplaceWhitespaceWith);
        invokeCreateParser(actual, expected);
    }

    public void testCreateParserMultiplePhraseQueriesStartAndEndOfString() throws Exception {
        String actual = "\"wheel chair\" something something else \"hi there\"";
        String expected = String.format("\"wheel%cchair\" something something else \"hi%cthere\"",
                DefaultReplaceWhitespaceWith, DefaultReplaceWhitespaceWith);
        invokeCreateParser(actual, expected);
    }

    public void testCreateParserMandatoryAndOptionalClause() throws Exception {
        String actual = "+mandatory -optional";
        String expected = actual;
        invokeCreateParser(actual, expected);
    }

    public void testCreateParserLocalParameters() throws Exception {
        String actual = "{!q.op=AND df=title}solr rocks";
        String expected = actual;
        invokeCreateParser(actual, expected);
    }

    public void testCreateParserRangeQueryToNow() throws Exception {
        String actual = "timestamp:[* TO NOW]";
        String expected = actual;
        invokeCreateParser(actual, expected);
    }

    public void testCreateParserRangeQueryToNowIgnoreCase() throws Exception {
        String actual = "timestamp:[* TO NOW]";
        String expected = actual;
        invokeCreateParser(actual, expected, true, DefaultReplaceWhitespaceWith);
    }

    public void testCreateParserRangeQueryDateToStar() throws Exception {
        String actual = "timestamp:[1976-03-06T23:59:59.999Z TO *]";
        String expected = actual;
        invokeCreateParser(actual, expected);
    }

    public void testCreateParserRangeQueryDateToDate() throws Exception {
        String actual = "timestamp:[1995-12-31T23:59:59.999Z TO 2007-03-06T00:00:00Z]";
        String expected = actual;
        invokeCreateParser(actual, expected);
    }

    public void testCreateParserRangeQueryDateMath() throws Exception {
        String actual = "timestamp:[NOW-1YEAR/DAY TO NOW/DAY+1DAY]";
        String expected = actual;
        invokeCreateParser(actual, expected);
    }

    public void testCreateParserGroupedNoPhrase() throws Exception {
        String actual = "(something that doesn't match)";
        String expected = actual;
        invokeCreateParser(actual, expected);
    }

    public void testCreateParserGroupedAutoPhrase() throws Exception {
        String actual = "(wheel chair)";
        String expected = String.format("(wheel%cchair)", DefaultReplaceWhitespaceWith);
        invokeCreateParser(actual, expected);
    }

    public void testCreateParserBooleanLogicIgnoreCase() throws Exception {
        String actual = "Wheel Chair AND something else OR hi there";
        String expected = String.format("wheel%cchair AND something else OR hi%cthere",
                DefaultReplaceWhitespaceWith, DefaultReplaceWhitespaceWith);
        invokeCreateParser(actual, expected, true, DefaultReplaceWhitespaceWith);
    }

    public void testCreateParserEmptyReplaceNoPhrase() throws Exception {
        String actual = "something";
        String expected = actual;
        invokeCreateParser(actual, expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
    }

    public void testCreateParserEmptyReplaceWithSpaceNoPhrase() throws Exception {
        String actual = "two things";
        String expected = actual;
        invokeCreateParser(actual, expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
    }

    public void testCreateParserEmptyReplaceWithPhrase() throws Exception {
        String actual = "wheel chair";
        String expected = "wheelchair";
        invokeCreateParser(actual, expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
    }

    public void testCreateParserEmptyReplaceMultiplePhrasesSomeMatch() throws Exception {
        String actual = "wheel chair something hi there";
        String expected = "wheelchair something hithere";
        invokeCreateParser(actual, expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
    }

    public void testCreateParserEmptyReplaceMultiplePhrasesInsideString() throws Exception {
        String actual = "something wheel chair hi there something else";
        String expected = "something wheelchair hithere something else";
        invokeCreateParser(actual, expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
    }

    public void testCreateParserMultipleSpacesInPhrase() throws Exception {
        String actual = "more than one space";
        String expected = "morethanonespace";
        invokeCreateParser(actual, expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
    }

    public void testCreateParserDuplicatePhrase() throws Exception {
        String actual = "dup licate";
        String expected = "duplicate";
        invokeCreateParser(actual, expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
    }

    public void test60Cc() throws Exception {
        String actual = "60 cc";
        String expected = "60cc";
        invokeCreateParser(actual, expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
    }

    public void test60Ml() throws Exception {
        String actual = "60 ml";
        String expected = "60ml";
        invokeCreateParser(actual, expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
    }

    public void test60Mm() throws Exception {
        String actual = "60 mm";
        String expected = "60mm";
        invokeCreateParser(actual, expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
    }

    public void test60Lb() throws Exception {
        String expected = "60lb";
        invokeCreateParser("60 lb", expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
        invokeCreateParser("60lb", expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
        invokeCreateParser("60 lbs", expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
        invokeCreateParser("60lbs", expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
        invokeCreateParser("60 pound", expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
        invokeCreateParser("60 pounds", expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
    }

    public void test60In() throws Exception {
        String expected = "60in";
        invokeCreateParser("60 in", expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
        invokeCreateParser("60in", expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
        invokeCreateParser("60 i", expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
        invokeCreateParser("60i", expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
        invokeCreateParser("60 inch", expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
        invokeCreateParser("60inches", expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
        invokeCreateParser("60 inches", expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
    }

    public void test60Oz() throws Exception {
        String expected = "60oz";
        invokeCreateParser("60 oz", expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
        invokeCreateParser("60oz", expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
        invokeCreateParser("60 ounce", expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
        invokeCreateParser("60 ounces", expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);

    }

    public void test60Qt() throws Exception {
        String expected = "60qt";
        invokeCreateParser("60 qt", expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
        invokeCreateParser("60qt", expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
        invokeCreateParser("60 qts", expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
        invokeCreateParser("60qts", expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
    }

    public void test60Gal() throws Exception {
        String expected = "60gal";
        invokeCreateParser("60 gal", expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
        invokeCreateParser("60gal", expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
        invokeCreateParser("60 ga", expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
        invokeCreateParser("60ga", expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
        invokeCreateParser("60 gallon", expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
        invokeCreateParser("60 gallons", expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
    }

    public void test60Yd() throws Exception {
        String expected = "60yd";
        invokeCreateParser("60 yd", expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
        invokeCreateParser("60yd", expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
        invokeCreateParser("60 yds", expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
        invokeCreateParser("60yds", expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
        invokeCreateParser("60 yard", expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
        invokeCreateParser("60 yards", expected, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
    }

    public void testCreateParserNullQuery() throws Exception {
        invokeCreateParser(null, null, DefaultIgnoreCase, EmptyReplaceWhitespaceWith);
    }

    private void invokeCreateParser(String query, String expectedModifiedQuery) throws IOException {
        invokeCreateParser(query, expectedModifiedQuery, DefaultIgnoreCase, DefaultReplaceWhitespaceWith);
    }

    private void invokeCreateParser(
            String query, String expectedModifiedQuery, boolean ignoreCase, Character replaceWhitespaceWith) throws IOException {

        AutoPhrasingQParserPlugin parser = getParserAndInvokeInit(ignoreCase, replaceWhitespaceWith);
        assertNotNull(parser);

        invokeInform(parser);

        SolrParams params = SolrParams.toSolrParams(getParams());
        SolrParams localParams = SolrParams.toSolrParams(new NamedList());

        SolrQueryRequest mockQueryRequest = Mockito.mock(SolrQueryRequest.class);
        final SolrCore mockSolrCore = PowerMockito.mock(SolrCore.class);
        QParserPlugin mockQueryPlugin = Mockito.mock(QParserPlugin.class);

        Mockito.when(mockQueryRequest.getCore()).thenReturn(mockSolrCore);
        PowerMockito.when(mockSolrCore.getQueryPlugin(DownstreamParser)).thenReturn(mockQueryPlugin);
        Mockito.when(mockQueryPlugin.createParser(
                Matchers.eq(expectedModifiedQuery), Matchers.any(SolrParams.class),
                Matchers.any(SolrParams.class), Matchers.any(SolrQueryRequest.class))).thenReturn(null);

        parser.createParser(query, params, localParams, mockQueryRequest);

        Mockito.verify(mockQueryPlugin).createParser(
                Matchers.eq(expectedModifiedQuery), Matchers.any(SolrParams.class),
                Matchers.any(SolrParams.class), Matchers.any(SolrQueryRequest.class));
    }

    public void testInform() throws Exception {
        AutoPhrasingQParserPlugin parser = getParserAndInvokeInit();

        List<String> expectedPhrases = invokeInform(parser);

        CharArraySet actualSet = parser.getPhrases();
        CharArraySet expectedSet = StopFilter.makeStopSet(Version.LUCENE_48, expectedPhrases, DefaultIgnoreCase);

        assertEquals(expectedSet.size(), actualSet.size());
        for (Object anExpected : expectedSet) {
            assertTrue(actualSet.contains(anExpected));
        }
    }

    private List<String> invokeInform(AutoPhrasingQParserPlugin parser) throws IOException {
        ResourceLoader mockResourceLoader = Mockito.mock(ResourceLoader.class);
        PowerMockito.mockStatic(WordlistLoader.class);

        List<String> expectedPhrases = getPhrases();
        Mockito.when(WordlistLoader.getLines((InputStream) Matchers.anyObject(), (Charset) Matchers.anyObject()))
                .thenReturn(expectedPhrases);

        parser.inform(mockResourceLoader);

        return expectedPhrases;
    }

    private AutoPhrasingQParserPlugin getParserAndInvokeInit() {
        return getParserAndInvokeInit(DefaultIgnoreCase, DefaultReplaceWhitespaceWith);
    }

    private AutoPhrasingQParserPlugin getParserAndInvokeInit(boolean ignoreCase, Character replaceWhitespaceWith) {
        AutoPhrasingQParserPlugin parser = new AutoPhrasingQParserPlugin();
        assertNotNull(parser);

        NamedList<java.io.Serializable> params = getParams(ignoreCase, replaceWhitespaceWith);
        parser.init(params);

        return parser;
    }

    private List<String> getPhrases() {
        List<String> phrases = new ArrayList<String>();
        phrases.add("hi there");
        phrases.add("wheel chair");
        phrases.add("more than one space");
        phrases.add("dup licate");
        phrases.add("dup licate");
        return phrases;
    }

    private NamedList<Serializable> getParams() {
        return getParams(DefaultIgnoreCase, DefaultReplaceWhitespaceWith);
    }

    private NamedList<Serializable> getParams(boolean ignoreCase, Character replaceWhitespaceWith) {

        NamedList<Serializable> params = new NamedList<Serializable>();
        params.add("defType", DownstreamParser);
        if (replaceWhitespaceWith != null) {
            params.add("replaceWhitespaceWith", replaceWhitespaceWith);
        }
        params.add("ignoreCase", ignoreCase);
        params.add("phrases", "phrases.txt");
        params.add("includeTokens", true);

        return params;
    }
}