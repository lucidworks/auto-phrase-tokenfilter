package com.lucidworks.analysis;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.util.CharArrayMap;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.util.ArrayList;

import static java.lang.System.arraycopy;
/**
 * Performs "auto phrasing" on a token stream. Auto phrases refer to sequences of tokens that
 * are meant to describe a single thing and should be searched for as such. When these phrases
 * are detected in the token stream, a single token representing the phrase is emitted rather than
 * the individual tokens that make up the phrase. The filter supports overlapping phrases.
 * <p/>
 * The Autophrasing filter can be combined with a synonym filter to handle cases in which prefix or
 * suffix terms in a phrase are synonymous with the phrase, but where other parts of the phrase are
 * not.
 */

@SuppressWarnings({"unchecked", "PrimitiveArrayArgumentToVariableArgMethod"})
public final class AutoPhrasingTokenFilter extends TokenFilter {

    private CharTermAttribute charTermAttr;
    private PositionIncrementAttribute positionIncrementAttr;
    private OffsetAttribute offsetAttr;

    private Character replaceWhitespaceWith = null;
    private Version luceneMatchVersion;

    // maps the first word in each auto phrase to all phrases that start with that word
    private CharArrayMap<CharArraySet> phraseMapFirstWordToPhrases;

    private ArrayList<char[]> unusedTokens = new ArrayList<char[]>();
    private int offsetStartPos = 0;
    private int offsetEndPos = 0;

    public AutoPhrasingTokenFilter(Version matchVersion, TokenStream input, CharArraySet phraseSet) {
        super(input);

        this.luceneMatchVersion = matchVersion;
        final int estimatedPhraseMapEntries = 100;
        phraseMapFirstWordToPhrases =
                new CharArrayMap<CharArraySet>(luceneMatchVersion, estimatedPhraseMapEntries, false);

        initializePhraseMap(phraseSet);
        initializeAttributes();
    }

    private void initializePhraseMap(CharArraySet phraseSet) {
        final int EstimatedPhrasesPerFirstWord = 5;

        for (Object aPhrase : phraseSet) {
            char[] phrase = (char[])aPhrase;
            char[] firstWord = CharArrayUtil.getFirstTerm(phrase);
            CharArraySet phrases = phraseMapFirstWordToPhrases.get(firstWord, 0, firstWord.length);
            if (phrases == null){
                phrases = new CharArraySet(luceneMatchVersion, EstimatedPhrasesPerFirstWord, false);
                phraseMapFirstWordToPhrases.put(firstWord, phrases);
            }
            phrases.add(phrase);
        }
    }

    private void initializeAttributes() {
        this.charTermAttr = addAttribute(CharTermAttribute.class);
        this.positionIncrementAttr = addAttribute(PositionIncrementAttribute.class);
        this.offsetAttr = addAttribute(OffsetAttribute.class);
    }

    public void setReplaceWhitespaceWith(Character replaceWhitespaceWith) {
        this.replaceWhitespaceWith = replaceWhitespaceWith;
    }

    @Override
    public void reset() throws IOException {
        unusedTokens.clear();
        offsetEndPos = 0;
        offsetStartPos = 0;
        super.reset();
    }

    @Override
    public boolean incrementToken() throws IOException {
        clearAttributes();

        for (char[] nextToken = nextToken() ; nextToken != null ; nextToken = nextToken()) {
            LazyLog.logDebug("Token to process: %s", nextToken);
            unusedTokens.add(nextToken);
        }

        if (unusedTokens.isEmpty()) {
            return false;
        }

        char[] firstWord = unusedTokens.get(0);
        CharArraySet potentialPhraseMatches = phraseMapFirstWordToPhrases.get(firstWord, 0, firstWord.length);

        if (potentialPhraseMatches == null) {
            LazyLog.logDebug("No potential phrase matches.");
            emit(unusedTokens.remove(0));
            return true;
        }

        //foreach potential phrase match look ahead in the queue and find the first match
        //remove those, make a phrase and emit it
        char[] phraseMatch = null;
        String[] phraseWords = null;
        for (Object aPotentialPhraseMatch : potentialPhraseMatches) {
            char[] potentialPhraseMatch = (char[])aPotentialPhraseMatch;
            String[] potentialPhraseWords = new String(potentialPhraseMatch).split(" ");

            if (potentialPhraseWords.length > unusedTokens.size())
                continue;

            boolean matches = true;
            for (int i = 0 ; i < potentialPhraseWords.length ; ++i) {
                if (!CharArrayUtil.equals(unusedTokens.get(i), potentialPhraseWords[i].toCharArray())) {
                    matches = false;
                    break;
                }
            }
            if (matches && (phraseMatch == null || potentialPhraseMatch.length > phraseMatch.length)) {
                LazyLog.logDebug("Found potential longest phrase match for '%s'.", potentialPhraseMatch);
                phraseMatch = new char[potentialPhraseMatch.length];
                arraycopy(potentialPhraseMatch, 0, phraseMatch, 0, potentialPhraseMatch.length);
                phraseWords = new String[potentialPhraseWords.length];
                arraycopy(potentialPhraseWords, 0, phraseWords, 0, potentialPhraseWords.length);
            }
        }
        if (phraseMatch != null) {
            LazyLog.logDebug("Found phrase match for '%s'.", phraseMatch);
            for (String ignored : phraseWords) {
                unusedTokens.remove(0);
            }

            emit(phraseMatch);
            return true;
        }

        LazyLog.logDebug("No phrase matches found, emitting single token.");
        emit(unusedTokens.remove(0));
        return true;
    }

    private char[] nextToken() throws IOException {
        if (input.incrementToken()) {
            if (charTermAttr != null) {
                char[] termBuf = charTermAttr.buffer();
                char[] nextTok = new char[charTermAttr.length()];
                arraycopy(termBuf, 0, nextTok, 0, charTermAttr.length());
                return nextTok;
            }
        }

        return null;
    }

    private void emit(char[] token) {
        LazyLog.logDebug("emit: '%s'", token);

        token = CharArrayUtil.replaceWhitespace(token, replaceWhitespaceWith);

        charTermAttr.setEmpty();
        charTermAttr.append(new StringBuilder().append(token));

        computeAndSetOffset(token.length);
    }

    private void computeAndSetOffset(int tokenLength) {
        offsetEndPos = offsetStartPos + tokenLength;
        offsetAttr.setOffset(offsetStartPos, offsetEndPos);
        offsetStartPos = offsetEndPos + 1;
    }
}
