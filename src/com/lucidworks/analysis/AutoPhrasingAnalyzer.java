package com.lucidworks.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

import java.io.Reader;

/**
 * Implements an Analyzer for the AutoPhrasingTokenFilter to assist in unit testing it
 */
public class AutoPhrasingAnalyzer extends Analyzer {

    private CharArraySet phraseSets;
    private boolean emitSingleTokens = false;
    private Character replaceWhitespaceWith = null;

    public AutoPhrasingAnalyzer(CharArraySet phraseSets, boolean emitSingleTokens) {
        this(phraseSets, emitSingleTokens, null);
    }

    public AutoPhrasingAnalyzer(CharArraySet phraseSets, boolean emitSingleTokens, Character replaceWhitespaceWith) {
        this.phraseSets = phraseSets;
        this.emitSingleTokens = emitSingleTokens;
        this.replaceWhitespaceWith = replaceWhitespaceWith;
    }

    @Override
    protected TokenStreamComponents createComponents(String s, Reader reader) {
        Tokenizer tokenizer = new WhitespaceTokenizer(Version.LUCENE_48, reader);
        AutoPhrasingTokenFilter tokenFilter =
                new AutoPhrasingTokenFilter(Version.LUCENE_48, tokenizer, phraseSets, emitSingleTokens);
        tokenFilter.setReplaceWhitespaceWith(replaceWhitespaceWith);
        return new TokenStreamComponents(tokenizer, tokenFilter);
    }
}
