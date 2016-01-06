package com.lucidworks.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import java.io.StringReader;

/**
 * Implements an Analyzer for the AutoPhrasingTokenFilter to assist in unit testing it
 */
public class AutoPhrasingAnalyzer extends Analyzer {

    private CharArraySet phraseSets;
    private Character replaceWhitespaceWith = null;

    public AutoPhrasingAnalyzer(CharArraySet phraseSets) {
        this(phraseSets, null);
    }

    public AutoPhrasingAnalyzer(CharArraySet phraseSets, Character replaceWhitespaceWith) {
        this.phraseSets = phraseSets;
        this.replaceWhitespaceWith = replaceWhitespaceWith;
    }

    @Override
    protected TokenStreamComponents createComponents(String s) {
        Tokenizer tokenizer = new WhitespaceTokenizer();
        tokenizer.setReader(new StringReader(s));
        AutoPhrasingTokenFilter tokenFilter =
                new AutoPhrasingTokenFilter(tokenizer, phraseSets);
        tokenFilter.setReplaceWhitespaceWith(replaceWhitespaceWith);
        return new TokenStreamComponents(tokenizer, tokenFilter);
    }
}
