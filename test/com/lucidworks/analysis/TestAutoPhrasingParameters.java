package com.lucidworks.analysis;

import java.io.Serializable;
import junit.framework.TestCase;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;

@SuppressWarnings("FieldCanBeLocal")
public class TestAutoPhrasingParameters extends TestCase {
    private final String DefaultDownstreamParser = "lucene";
    private final Character DefaultReplaceWhitespaceWith = null;
    private final boolean DefaultIgnoreCase = true;
    private final boolean DefaultEmitSingleTokens = false;
    private final String DefaultPhraseSetFiles = null;

    private final String DownstreamParser = "edismax";
    private final Character ReplaceWhitespaceWith = 'Z';
    private final boolean IgnoreCase = false;
    private final String PhrasesOneFile = "phrases.txt";
    private final String PhrasesMultipleFiles = "phrases.txt,more_phrases.txt";
    private final boolean EmitSingleTokens = true;

    public void testConstructorNullSolrParams(){
        AutoPhrasingParameters autoPhrasingParameters = new AutoPhrasingParameters(null);
        assertNotNull(autoPhrasingParameters);
        assertEquals(DefaultDownstreamParser, autoPhrasingParameters.getDownstreamParser());
        assertEquals(DefaultReplaceWhitespaceWith, autoPhrasingParameters.getReplaceWhitespaceWith());
        assertEquals(DefaultIgnoreCase, autoPhrasingParameters.getIgnoreCase());
        assertEquals(DefaultPhraseSetFiles, autoPhrasingParameters.getPhraseSetFiles());
        assertEquals(0, autoPhrasingParameters.getIndividualPhraseSetFiles().size());
        assertEquals(DefaultEmitSingleTokens, autoPhrasingParameters.getEmitSingleTokens());
    }

    public void testConstructorWithSolrParams() {
        SolrParams solrParams = getSolrParamsOnePhraseFile();
        AutoPhrasingParameters autoPhrasingParameters = new AutoPhrasingParameters(solrParams);
        assertNotNull(autoPhrasingParameters);
        assertEquals(DownstreamParser, autoPhrasingParameters.getDownstreamParser());
        assertEquals(ReplaceWhitespaceWith, autoPhrasingParameters.getReplaceWhitespaceWith());
        assertEquals(IgnoreCase, autoPhrasingParameters.getIgnoreCase());
        assertEquals(PhrasesOneFile, autoPhrasingParameters.getPhraseSetFiles());
        assertEquals(1, autoPhrasingParameters.getIndividualPhraseSetFiles().size());
        assertEquals(PhrasesOneFile, autoPhrasingParameters.getIndividualPhraseSetFiles().get(0));
        assertEquals(EmitSingleTokens, autoPhrasingParameters.getEmitSingleTokens());
    }

    public void testConstructorNoIgnoreCase(){
        SolrParams solrParams = getSolrParamsNoIgnoreCase();
        AutoPhrasingParameters autoPhrasingParameters = new AutoPhrasingParameters(solrParams);
        assertNotNull(autoPhrasingParameters);
        assertEquals(DownstreamParser, autoPhrasingParameters.getDownstreamParser());
        assertEquals(ReplaceWhitespaceWith, autoPhrasingParameters.getReplaceWhitespaceWith());
        assertEquals(DefaultIgnoreCase, autoPhrasingParameters.getIgnoreCase());
        assertEquals(PhrasesOneFile, autoPhrasingParameters.getPhraseSetFiles());
        assertEquals(1, autoPhrasingParameters.getIndividualPhraseSetFiles().size());
        assertEquals(PhrasesOneFile, autoPhrasingParameters.getIndividualPhraseSetFiles().get(0));
        assertEquals(EmitSingleTokens, autoPhrasingParameters.getEmitSingleTokens());
    }

    public void testConstructorNoEmitSingleTokens(){
        SolrParams solrParams = getSolrParamsNoEmitSingleTokens();
        AutoPhrasingParameters autoPhrasingParameters = new AutoPhrasingParameters(solrParams);
        assertNotNull(autoPhrasingParameters);
        assertEquals(DownstreamParser, autoPhrasingParameters.getDownstreamParser());
        assertEquals(ReplaceWhitespaceWith, autoPhrasingParameters.getReplaceWhitespaceWith());
        assertEquals(IgnoreCase, autoPhrasingParameters.getIgnoreCase());
        assertEquals(1, autoPhrasingParameters.getIndividualPhraseSetFiles().size());
        assertEquals(PhrasesOneFile, autoPhrasingParameters.getPhraseSetFiles());
        assertEquals(PhrasesOneFile, autoPhrasingParameters.getIndividualPhraseSetFiles().get(0));
        assertEquals(DefaultEmitSingleTokens, autoPhrasingParameters.getEmitSingleTokens());
    }

    public void testConstructorWithSolrParamsMultiplePhraseSetFiles() {
        SolrParams solrParams = getSolrParamsMultiplePhraseFiles();
        AutoPhrasingParameters autoPhrasingParameters = new AutoPhrasingParameters(solrParams);
        assertNotNull(autoPhrasingParameters);
        assertEquals(DownstreamParser, autoPhrasingParameters.getDownstreamParser());
        assertEquals(ReplaceWhitespaceWith, autoPhrasingParameters.getReplaceWhitespaceWith());
        assertEquals(IgnoreCase, autoPhrasingParameters.getIgnoreCase());
        assertEquals(PhrasesMultipleFiles, autoPhrasingParameters.getPhraseSetFiles());
        assertEquals(2, autoPhrasingParameters.getIndividualPhraseSetFiles().size());
        assertEquals("phrases.txt", autoPhrasingParameters.getIndividualPhraseSetFiles().get(0));
        assertEquals("more_phrases.txt", autoPhrasingParameters.getIndividualPhraseSetFiles().get(1));
    }

    private SolrParams getSolrParamsOnePhraseFile() {
        NamedList<Serializable> params  = new NamedList<Serializable>();
        params.add("defType", DownstreamParser);
        params.add("replaceWhitespaceWith", ReplaceWhitespaceWith);
        params.add("ignoreCase", IgnoreCase);
        params.add("phrases", PhrasesOneFile);
        params.add("includeTokens", EmitSingleTokens);

        return SolrParams.toSolrParams(params);
    }

    private SolrParams getSolrParamsMultiplePhraseFiles() {
        NamedList<Serializable> params  = new NamedList<Serializable>();
        params.add("defType", DownstreamParser);
        params.add("replaceWhitespaceWith", ReplaceWhitespaceWith);
        params.add("ignoreCase", IgnoreCase);
        params.add("phrases", PhrasesMultipleFiles);
        params.add("includeTokens", EmitSingleTokens);

        return SolrParams.toSolrParams(params);
    }

    private SolrParams getSolrParamsNoIgnoreCase() {
        NamedList<Serializable> params  = new NamedList<Serializable>();
        params.add("defType", DownstreamParser);
        params.add("replaceWhitespaceWith", ReplaceWhitespaceWith);
        params.add("phrases", PhrasesOneFile);
        params.add("includeTokens", EmitSingleTokens);

        return SolrParams.toSolrParams(params);
    }

    private SolrParams getSolrParamsNoEmitSingleTokens() {
        NamedList<Serializable> params  = new NamedList<Serializable>();
        params.add("defType", DownstreamParser);
        params.add("replaceWhitespaceWith", ReplaceWhitespaceWith);
        params.add("ignoreCase", IgnoreCase);
        params.add("phrases", PhrasesOneFile);

        return SolrParams.toSolrParams(params);
    }
}