package com.lucidworks.analysis;

import junit.framework.TestCase;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.WordlistLoader;
import org.apache.lucene.util.Version;
import org.apache.solr.common.util.NamedList;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for the AutoPhrasingQParserPlugin
 * Note: The use of PowerMock with java 1.7.0_65 will blow up with a "Error exists in the bytecode" type message
 *       More info at: https://code.google.com/p/powermock/issues/detail?id=504
 *       Workaround is to add the -noverify vm option to the test run configuration
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(WordlistLoader.class)
public class TestAutoPhrasingQParserPlugin extends TestCase {

    public void testCreateParser() throws Exception {
        //TODO: finish this
    }

    public void testInform() throws Exception {
        ResourceLoader mockResourceLoader = Mockito.mock(ResourceLoader.class);

        PowerMockito.mockStatic(WordlistLoader.class);

        List<String> expectedPhrases = getPhrases();
        Mockito.when(WordlistLoader.getLines((InputStream) Matchers.anyObject(), (Charset) Matchers.anyObject()))
            .thenReturn(expectedPhrases);

        AutoPhrasingQParserPlugin parser = getParser();
        parser.inform(mockResourceLoader);

        List<String> actualPhrases = parser.getPhrases();
        assertEquals(expectedPhrases.size(), actualPhrases.size());
        //TODO: assert contents perhaps by calling StopFilter.makeStopSet
    }

    private AutoPhrasingQParserPlugin getParser() {
        AutoPhrasingQParserPlugin parser = new AutoPhrasingQParserPlugin();
        assertNotNull(parser);

        NamedList params = getParams();
        parser.init(params);

        return parser;
    }

    private List<String> getPhrases(){
        List<String> phrases = new ArrayList<String>();
        phrases.add("hi");
        phrases.add("there");
        return phrases;
    }

    private NamedList getParams() {

        NamedList params  = new NamedList();
        params.add("defType", "edismax");
        params.add("replaceWhitespaceWith", 'Z');
        params.add("ignoreCase", false);
        params.add("phrases", "phrases.txt");
        params.add("includeTokens", true);

        return params;
    }
}