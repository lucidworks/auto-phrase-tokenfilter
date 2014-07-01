package com.lucidworks.analysis;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenFilterFactory;

public class AutoPhrasingTokenFilterFactory extends TokenFilterFactory implements ResourceLoaderAware {
	
    private CharArraySet phraseSets;
	private final String phraseSetFiles;
    private final boolean ignoreCase;
    private final boolean emitSingleTokens;

	public AutoPhrasingTokenFilterFactory(Map<String, String> initArgs) {
		super( initArgs );
		phraseSetFiles = get(initArgs, "phrases");
	    ignoreCase = getBoolean( initArgs, "ignoreCase", false);
	    emitSingleTokens = getBoolean( initArgs, "includeTokens", false );
	}

	@Override
	public void inform(ResourceLoader loader) throws IOException {
	    if (phraseSetFiles != null) {
	        phraseSets = getWordSet(loader, phraseSetFiles, ignoreCase);
	    }
	}
	

	@Override
	public TokenStream create( TokenStream input ) {
		AutoPhrasingTokenFilter autoPhraseFilter = new AutoPhrasingTokenFilter(luceneMatchVersion, input, phraseSets, emitSingleTokens );
	    return autoPhraseFilter;
	}

}
