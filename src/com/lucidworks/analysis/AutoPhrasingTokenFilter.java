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

    // The list of auto-phrase character strings
    private CharArrayMap<CharArraySet> phraseMap;

    // Set of first term in phrase to phrase(s) to be checked
    private CharArraySet currentSetToCheck = null;

    // The current phrase that has been seen in the token stream
    // since the first term match was encountered
    private StringBuffer currentPhrase = new StringBuffer();

    // Queue to allow old tokens that ultimately did not match to be
    // emitted before new tokens are emitted so that the filter can
    // work 'transparently'
    private ArrayList<Token> unusedTokens = new ArrayList<Token>();

    private char[] lastToken = null;
    private char[] lastEmitted = null;
    private char[] lastValid = null;

    private Character replaceWhitespaceWith = null;

    private int positionIncrement = 0;
    private int lastEndPos = 0;
    private int lastStartPos = 0;

    @SuppressWarnings("UnusedParameters")
    public AutoPhrasingTokenFilter(Version matchVersion, TokenStream input, CharArraySet phraseSet) {
        super(input);

        initializeAttributes();

        // Convert to CharArrayMap by iterating the char[] strings and
        // putting them into the CharArrayMap with Integer of the number
        // of tokens in the map: need this to determine when a phrase match is completed.
        this.phraseMap = convertPhraseSet(phraseSet);
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
        currentSetToCheck = null;
        currentPhrase.setLength(0);
        lastToken = null;
        lastEmitted = null;
        unusedTokens.clear();
        positionIncrement = 0;
        super.reset();
    }

    @Override
    public boolean incrementToken() throws IOException {
        clearAttributes();

        if (unusedTokens.size() > 0) {
            // emit these until the queue is empty before emitting any new stuff
            Token aToken = unusedTokens.remove(0);
            LazyLog.logDebug("Emitting unused token: %s", aToken.tok);
            emit(aToken);
            return true;
        }

        if (lastToken != null) {
            LazyLog.logDebug("Emitting last token: %s", lastToken);
            emit(lastToken);
            lastToken = null;
            return true;
        }

        char[] nextToken = nextToken();
        saveOffsets();
        LazyLog.logDebug("nextToken: %s", nextToken);

        if (nextToken == null) {
            // restore offsets
            offsetAttr.setOffset(lastStartPos, lastEndPos);

            if (lastValid != null) {
                LazyLog.logDebug("Emitting last valid token: %s", lastValid);
                emit(lastValid);
                lastValid = null;
                return true;
            }

            if (currentSetToCheck != null && currentSetToCheck.size() > 0) {
                if (lastEmitted != null && !CharArrayUtil.equals(fixWhitespace(lastEmitted), getCurrentBuffer(new char[0]))) {
                    discardCharTokens(currentPhrase, unusedTokens);
                    currentSetToCheck = null;
                    if (unusedTokens.size() > 0) {
                        Token aToken = unusedTokens.remove(0);
                        LazyLog.logDebug("emitting put back token");
                        emit(aToken);
                        return true;
                    }
                }
            }

            if (lastEmitted == null && (currentPhrase != null && currentPhrase.length() > 0)) {
                char[] lastTok = getCurrentBuffer(new char[0]);
                if (currentSetToCheck.contains(lastTok, 0, lastTok.length)) {
                    emit(lastTok);
                    currentPhrase.setLength(0);
                    return true;
                } else {
                    discardCharTokens(currentPhrase, unusedTokens);
                    currentSetToCheck = null;
                    currentPhrase.setLength(0);
                    if (unusedTokens.size() > 0) {
                        Token aToken = unusedTokens.remove(0);
                        LazyLog.logDebug("emitting put back token");
                        emit(aToken);
                        return true;
                    }
                }
            }
            return false;
        } // nextToken == null

        if (currentSetToCheck == null || currentSetToCheck.size() == 0) {
            LazyLog.logDebug("Checking for phrase start on '%s'", nextToken);

            if (phraseMap.keySet().contains(nextToken, 0, nextToken.length)) {
                LazyLog.logDebug("Found phrase start on '%s'", nextToken);
                // get the phrase set for this token, add it to current set to check
                currentSetToCheck = phraseMap.get(nextToken, 0, nextToken.length);
                if (currentPhrase == null) currentPhrase = new StringBuffer();
                else currentPhrase.setLength(0);
                currentPhrase.append(nextToken);

                LazyLog.logDebug("Calling incrementToken() recursively.");
                return incrementToken();
            } else {
                LazyLog.logDebug("No phrase match on '%s'", nextToken);
                emit(nextToken);
                lastToken = null;
                return true;
            }
        } else {
            // add token to the current string buffer.
            char[] currentBuffer = getCurrentBuffer(nextToken);

            if (currentSetToCheck.contains(currentBuffer, 0, currentBuffer.length)) {
                // if its the only one valid, emit it
                // if there is a longer one, wait to see if it will be matched
                // if the longer one breaks on the next token, emit this one...
                // emit the current phrase
                currentSetToCheck = remove(currentBuffer);

                if (currentSetToCheck.size() == 0) {
                    emit(currentBuffer);
                    lastValid = null;
                    --positionIncrement;
                    LazyLog.logDebug("Decremented the position increment to: %d", positionIncrement);
                } else {
                    lastValid = currentBuffer;
                }

                if (phraseMap.keySet().contains(nextToken, 0, nextToken.length)) {
                    // get the phrase set for this token, add it to current phrases to check
                    currentSetToCheck = phraseMap.get(nextToken, 0, nextToken.length);
                    if (currentPhrase == null) currentPhrase = new StringBuffer();
                    else currentPhrase.setLength(0);
                    currentPhrase.append(nextToken);
                }

                return (lastValid == null) || incrementToken();
            }

            if (phraseMap.keySet().contains(nextToken, 0, nextToken.length)) {
                // get the phrase set for this token, add it to current phrases to check
                // System.out.println( "starting new phrase with " + new String( nextToken ) );
                // does this add all of the set? if not need iterator loop
                CharArraySet newSet = phraseMap.get(nextToken, 0, nextToken.length);
                for (Object aNewSet : newSet) {
                    char[] phrase = (char[]) aNewSet;
                    currentSetToCheck.add(phrase);
                }
            }

            // for each phrase in currentSetToCheck -
            // if there is a phrase prefix match, get the next token recursively
            for (Object aCurrentSetToCheck : currentSetToCheck) {
                char[] phrase = (char[]) aCurrentSetToCheck;

                if (CharArrayUtil.startsWith(phrase, currentBuffer)) {
                    return incrementToken();
                }
            }

            if (lastValid != null) {
                emit(lastValid);
                lastValid = null;
                return true;
            }


            // current phrase didn't match fully: put the tokens back
            // into the unusedTokens list
            discardCharTokens(currentPhrase, unusedTokens);
            currentPhrase.setLength(0);
            currentSetToCheck = null;

            if (unusedTokens.size() > 0) {
                Token aToken = unusedTokens.remove(0);
                LazyLog.logDebug("emitting put back token");
                emit(aToken);
                return true;
            }

            currentSetToCheck = null;

            LazyLog.logDebug("returning at end.");
            return incrementToken();
        }
    }

    private void saveOffsets() {
        if (offsetAttr.startOffset() > 0) lastStartPos = offsetAttr.startOffset();
        if (offsetAttr.endOffset() > 0) lastEndPos = offsetAttr.endOffset();
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

    private char[] getCurrentBuffer(char[] newToken) {
        if (currentPhrase == null) currentPhrase = new StringBuffer();
        if (newToken != null && newToken.length > 0) {
            if (currentPhrase.length() > 0) currentPhrase.append(' ');
            currentPhrase.append(newToken);
        }

        char[] currentBuff = new char[currentPhrase.length()];
        currentPhrase.getChars(0, currentPhrase.length(), currentBuff, 0);
        return currentBuff;
    }


    private void emit(char[] token) {
        LazyLog.logDebug("emit: %s", token);

        token = replaceWhiteSpace(token);

        charTermAttr.setEmpty();
        charTermAttr.append(new StringBuilder().append(token));

        if (offsetAttr != null && offsetAttr.endOffset() >= token.length) {
            int start = offsetAttr.endOffset() - token.length;
            offsetAttr.setOffset(start, offsetAttr.endOffset());
            saveOffsets();
        }

        positionIncrementAttr.setPositionIncrement(++positionIncrement);

        lastEmitted = token;
    }

    private void emit(Token token) {
        emit(token.tok);
        if (token.endPos > token.startPos && token.startPos >= 0) {
            offsetAttr.setOffset(token.startPos, token.endPos);
            saveOffsets();
        }
    }

    // replaces whitespace char with replaceWhitespaceWith
    private char[] replaceWhiteSpace(char[] token) {
        char[] replaced = new char[token.length];
        int srcPos, destPos;
        for (srcPos = 0, destPos = 0; srcPos < token.length; srcPos++) {
            if (token[srcPos] == ' ') {
                if (replaceWhitespaceWith == null) {
                    continue;
                }
                replaced[destPos++] = replaceWhitespaceWith;
            } else {
                replaced[destPos++] = token[srcPos];
            }
        }
        if (srcPos != destPos){
            char[] shorterReplaced = new char[destPos];
            System.arraycopy(replaced, 0, shorterReplaced, 0, destPos);
            return shorterReplaced;
        }
        return replaced;
    }

    private CharArrayMap convertPhraseSet(CharArraySet phraseSet) {
        CharArrayMap<CharArraySet> phraseMap = new CharArrayMap(Version.LUCENE_48, 100, false);
        for (Object aPhraseSet : phraseSet) {
            char[] phrase = (char[]) aPhraseSet;

            LazyLog.logDebug("'%s'", phrase);

            char[] firstTerm = getFirstTerm(phrase);
            LazyLog.logDebug("'%s'", firstTerm);

            CharArraySet itsPhrases = phraseMap.get(firstTerm, 0, firstTerm.length);
            if (itsPhrases == null) {
                itsPhrases = new CharArraySet(Version.LUCENE_48, 5, false);
                phraseMap.put(new String(firstTerm), itsPhrases);
            }

            itsPhrases.add(phrase);
        }

        return phraseMap;
    }

    private char[] getFirstTerm(char[] phrase) {
        int spNdx = 0;
        while (spNdx < phrase.length) {
            if (isSpaceChar(phrase[spNdx++])) {
                break;
            }
        }

        char[] firstCh = new char[spNdx - 1];
        arraycopy(phrase, 0, firstCh, 0, spNdx - 1);
        return firstCh;
    }

    private boolean isSpaceChar(char ch) {
        return " \t\n\r".indexOf(ch) >= 0;
    }

    // reconstruct the unused tokens from the phrase (since it didn't match)
    // need to recompute the token positions based on the length of the currentPhrase,
    // the current ending position and the length of each token.
    private void discardCharTokens(StringBuffer phrase, ArrayList<Token> tokenList) {
        LazyLog.logDebug("discardCharTokens: '%s'", phrase);
        int endPos = offsetAttr.endOffset();
        if (endPos == 0)
            endPos = lastEndPos;
        int startPos = endPos - phrase.length();

        int lastSp = 0;
        for (int i = 0; i < phrase.length(); i++) {
            char chAt = phrase.charAt(i);
            if (isSpaceChar(chAt) && i > lastSp) {
                char[] tok = new char[i - lastSp];
                phrase.getChars(lastSp, i, tok, 0);
                if (lastEmitted == null || !CharArrayUtil.endsWith(lastEmitted, tok)) {
                    Token token = new Token();
                    token.tok = tok;

                    token.startPos = startPos + lastSp;
                    token.endPos = token.startPos + tok.length;
                    LazyLog.logDebug("discard %s: %d, %d", tok, token.startPos, token.endPos);
                    tokenList.add(token);
                }
                lastSp = i + 1;
            }
        }
        char[] tok = new char[phrase.length() - lastSp];
        phrase.getChars(lastSp, phrase.length(), tok, 0);

        Token token = new Token();
        token.tok = tok;
        token.endPos = endPos;
        token.startPos = endPos - tok.length;
        tokenList.add(token);
    }

    private CharArraySet remove(char[] charArray) {
        CharArraySet newSet = new CharArraySet(Version.LUCENE_48, 5, false);
        for (Object aCurrentSetToCheck : currentSetToCheck) {
            char[] phrase = (char[]) aCurrentSetToCheck;

            /* There is a bug here for overlapping phrases when emitting single tokens
             *  the implementation seems to conflict with itself for overlapping phrases at the start and end of a token stream
             *  and it worked originally because of a bug with endsWith that cause a worse thing to happen. */
            if (!CharArrayUtil.equals(charArray, phrase)/* && !(CharArrayUtil.startsWith(charArray, phrase) || CharArrayUtil.endsWith(charArray, phrase))*/) {
                newSet.add(phrase);
            }
        }

        return newSet;
    }

    private char[] fixWhitespace(char[] phrase) {
        if (replaceWhitespaceWith == null) return phrase;
        char[] fixed = new char[phrase.length];
        for (int i = 0; i < phrase.length; i++) {
            if (phrase[i] == replaceWhitespaceWith) {
                fixed[i] = ' ';
            } else {
                fixed[i] = phrase[i];
            }
        }
        return fixed;
    }

    class Token {
        char[] tok;
        int startPos;
        int endPos;
    }
}
