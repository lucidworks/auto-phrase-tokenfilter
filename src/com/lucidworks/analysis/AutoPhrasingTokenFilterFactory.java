package com.lucidworks.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenFilterFactory;

import java.io.IOException;
import java.util.Map;

public class AutoPhrasingTokenFilterFactory extends TokenFilterFactory implements ResourceLoaderAware {

    private CharArraySet phraseSets;
    private final String phraseSetFiles;
    private final boolean ignoreCase;
    private final boolean emitSingleTokens;

    private String replaceWhitespaceWith = null;

    public AutoPhrasingTokenFilterFactory(Map<String, String> initArgs) {
        super(initArgs);
        phraseSetFiles = get(initArgs, "phrases");
        ignoreCase = getBoolean(initArgs, "ignoreCase", false);
        emitSingleTokens = getBoolean(initArgs, "includeTokens", false);

        String replaceWhitespaceArg = initArgs.get("replaceWhitespaceWith");
        if (replaceWhitespaceArg != null) {
            replaceWhitespaceWith = replaceWhitespaceArg;
        }
    }


    @Override
    public void inform(ResourceLoader loader) throws IOException {
        if (phraseSetFiles != null) {
            phraseSets = getWordSet(loader, phraseSetFiles, ignoreCase);
        }
    }


    @Override
    public TokenStream create(TokenStream input) {
        AutoPhrasingTokenFilter autoPhraseFilter = new AutoPhrasingTokenFilter(luceneMatchVersion, input, phraseSets, emitSingleTokens);
        if (replaceWhitespaceWith != null) {
            autoPhraseFilter.setReplaceWhitespaceWith(replaceWhitespaceWith.charAt(0));
        }
        return autoPhraseFilter;
    }

}
