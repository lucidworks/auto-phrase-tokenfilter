package com.lucidworks.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenFilterFactory;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class AutoPhrasingTokenFilterFactory extends TokenFilterFactory implements ResourceLoaderAware {

    private AutoPhrasingParameters autoPhrasingParameters;
    private CharArraySet phraseSets;

    public AutoPhrasingTokenFilterFactory(Map<String, String> initArgs) {
        super(initArgs);

        SolrParams params = SolrParams.toSolrParams(new NamedList(initArgs));
        autoPhrasingParameters = new AutoPhrasingParameters(params);
    }

    @Override
    public void inform(ResourceLoader loader) throws IOException {
        String phraseSetFiles = autoPhrasingParameters.getPhraseSetFiles();
        boolean ignoreCase = autoPhrasingParameters.getIgnoreCase();

        if (phraseSetFiles != null)
            phraseSets = getWordSet(loader, phraseSetFiles, ignoreCase);
    }

    @Override
    public TokenStream create(TokenStream input) {
        boolean emitSingleTokens = autoPhrasingParameters.getEmitSingleTokens();
        char replaceWhitespaceWith = autoPhrasingParameters.getReplaceWhitespaceWith();

        AutoPhrasingTokenFilter autoPhraseFilter = new AutoPhrasingTokenFilter(
                luceneMatchVersion, input, phraseSets, emitSingleTokens);
        autoPhraseFilter.setReplaceWhitespaceWith(replaceWhitespaceWith);
        return autoPhraseFilter;
    }

}
