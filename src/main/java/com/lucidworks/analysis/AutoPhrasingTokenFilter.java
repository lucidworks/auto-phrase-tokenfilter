package com.lucidworks.analysis;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.CharArrayMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs "auto phrasing" on a token stream. Auto phrases refer to sequences of tokens that
 * are meant to describe a single thing and should be searched for as such. When these phrases
 * are detected in the token stream, a single token representing the phrase is emitted rather than
 * the individual tokens that make up the phrase. The filter supports overlapping phrases.
 *
 * The Autophrasing filter can be combined with a synonym filter to handle cases in which prefix or
 * suffix terms in a phrase are synonymous with the phrase, but where other parts of the phrase are
 * not.
 */

public class AutoPhrasingTokenFilter extends TokenFilter {

  private static final Logger Log = LoggerFactory.getLogger( AutoPhrasingTokenFilter.class );

  // The list of auto-phrase character strings
  private CharArrayMap<CharArraySet> phraseMap;

  // Set of first term in phrase to phrase(s) to be checked
  private LinkedHashSet<String> currentSetToCheck = null;

  // The current phrase that has been seen in the token stream
  // since the first term match was encountered
  private StringBuilder currentPhrase = new StringBuilder( );

  // Queue to allow old tokens that ultimately did not match to be
  // emitted before new tokens are emitted so that the filter can
  // work 'transparently'
  private LinkedList<Token> unusedTokens = new LinkedList<>();

  // If true - emit single tokens as well as auto-phrases
  private boolean emitSingleTokens;

  private String lastToken = null;
  private String lastEmitted = null;
  private String lastValid = null;

  private Character replaceWhitespaceWith = null;

  private int positionIncr = 0;

  public AutoPhrasingTokenFilter(TokenStream input, CharArrayMap<CharArraySet> phraseMap, boolean emitSingleTokens)   {
    super(input);

    // Convert to CharArrayMap by iterating the char[] strings and
    // putting them into the CharArrayMap with Integer of the number
    // of tokens in the map: need this to determine when a phrase match is completed.
    this.phraseMap = phraseMap;
    this.emitSingleTokens = emitSingleTokens;
  }

  public AutoPhrasingTokenFilter(TokenStream input, CharArraySet phraseSet, boolean emitSingleTokens ) {
    this(input, convertPhraseSet(phraseSet), emitSingleTokens);
  }

  protected AutoPhrasingTokenFilter(TokenStream input) {
    super( input );
  }

  public void setReplaceWhitespaceWith( Character replaceWhitespaceWith ) {
    this.replaceWhitespaceWith = replaceWhitespaceWith;
  }


  @Override
  public void reset( )  throws IOException {
    currentSetToCheck = null;
    currentPhrase.setLength( 0 );
    lastToken = null;
    lastEmitted = null;
    unusedTokens.clear( );
    positionIncr = 0;
    super.reset();
  }

  @Override
  public final boolean incrementToken() throws IOException {
    if (!emitSingleTokens && unusedTokens.size() > 0) {
      Log.debug( "emitting unused phrases 1" );
      // emit these until the queue is empty before emitting any new stuff
      Token aToken = unusedTokens.removeFirst();
      emit( aToken );
      return true;
    }

    if (lastToken != null) {
      Log.debug( "emit lastToken" );
      emit( lastToken );
      lastToken = null;
      return true;
    }

    String nextToken = nextToken( );
    // if (nextToken != null) System.out.println( "nextToken: " + new String( nextToken ));
    if (nextToken == null) {
      if (lastValid != null) {
        Log.debug( "emit lastValid" );
        emit( lastValid );
        lastValid = null;
        return true;
      }

      if (emitSingleTokens && currentSetToCheck != null && currentSetToCheck.size() > 0) {
        String phrase = getFirst( currentSetToCheck );
        StringBuilder lastTok = getCurrentBuffer();
        if (phrase != null && endsWith( lastTok, phrase)) {
          currentSetToCheck = remove( currentSetToCheck, phrase );
          Log.debug( "emit phrase" );
          emit( phrase );
          return true;
        }
      }
      else if (!emitSingleTokens && currentSetToCheck != null && currentSetToCheck.size() > 0) {
        StringBuilder currBuff = getCurrentBuffer(null);
        if (lastEmitted != null && !equalsWithFixedWhiteSpace( lastEmitted, currBuff )) {
          discardCharTokens( currentPhrase, unusedTokens );
          currentSetToCheck = null;
          if (unusedTokens.size() > 0) {
            Token aToken = unusedTokens.remove( 0 );
            // don't emit if current phrase not completed and overlaps with lastEmitted
            if (!endsWith( lastEmitted, currBuff )) {
              Log.debug( "emitting putback token 2");
              emit( aToken );
              return true;
            }
          }
        }
      }

      if (lastEmitted == null && (currentPhrase != null && currentPhrase.length() > 0)) {
        StringBuilder lastTok = getCurrentBuffer();
        if (currentSetToCheck.contains( lastTok.toString() )) {
          Log.debug( "emit lastTok " );
          emit( lastTok );
          currentPhrase.setLength( 0 );
          return true;
        }

        else if (!emitSingleTokens) {
          discardCharTokens( currentPhrase, unusedTokens );
          currentSetToCheck = null;
          currentPhrase.setLength( 0 );
          if (unusedTokens.size() > 0) {
            Token aToken = unusedTokens.remove( 0 );
            Log.debug( "emitting putback token 3");
            emit( aToken );
            return true;
          }
        }
      }
      return false;
    }

    // if emitSingleToken, set lastToken = nextToken
    if (emitSingleTokens) {
      lastToken = nextToken;
    }

    if (currentSetToCheck == null || currentSetToCheck.isEmpty() ) {
      Log.debug( "Checking for phrase start on '" + nextToken + "'" );

      CharArraySet charArraySet = phraseMap.get(nextToken);
      if (charArraySet != null) {
        // get the phrase set for this token, add it to currentSetTocheck
        currentSetToCheck = convertToStringSet(charArraySet);
        if (currentPhrase == null) currentPhrase = new StringBuilder( );
        else currentPhrase.setLength( 0 );
        currentPhrase.append( nextToken );
        return incrementToken( );
      }
      else {
        Log.debug( "emit nextToken" );
        emit( nextToken );
        // clear lastToken
        lastToken = null;
        return true;
      }
    }
    else {
      // add token to the current string buffer.
      String currentBuffer = getCurrentBuffer( nextToken ).toString();

      if (currentSetToCheck.contains( currentBuffer )) {
        // if its the only one valid, emit it
        // if there is a longer one, wait to see if it will be matched
        // if the longer one breaks on the next token, emit this one...
        // emit the current phrase
        currentSetToCheck = remove( currentSetToCheck, currentBuffer );

        if (currentSetToCheck.size() == 0) {
          emit( currentBuffer );
          lastValid = null;
          --positionIncr;
        }
        else {
          if (emitSingleTokens) {
            lastToken = currentBuffer;
            return true;
          }
          lastValid = currentBuffer;
        }

        CharArraySet charArraySet = phraseMap.get(nextToken);
        if (charArraySet != null) {
          // get the phrase set for this token, add it to currentPhrasesTocheck
          currentSetToCheck = convertToStringSet(charArraySet);
          if (currentPhrase == null) currentPhrase = new StringBuilder( );
          else currentPhrase.setLength( 0 );
          currentPhrase.append( nextToken );
        }

        return (lastValid != null) ? incrementToken() : true;
      }

      CharArraySet newSet = phraseMap.get(nextToken);
      if (newSet != null) {
        // get the phrase set for this token, add it to currentPhrasesTocheck
        // System.out.println( "starting new phrase with " + new String( nextToken ) );
        // does this add all of the set? if not need iterator loop
        Iterator<Object> phraseIt = newSet.iterator();
        while (phraseIt.hasNext() ) {
          char[] phrase = (char[])phraseIt.next();
          currentSetToCheck.add( new String(phrase) );
        }
      }

      // for each phrase in currentSetToCheck -
      // if there is a phrase prefix match, get the next token recursively
      Iterator<String> phraseIt = currentSetToCheck.iterator();
      while (phraseIt.hasNext() ) {
        String phrase = phraseIt.next();

        if (startsWith( phrase, currentBuffer )) {
          return incrementToken( );
        }
      }

      if (lastValid != null) {
        Log.debug( "emit lastValid" );
        emit( lastValid );
        lastValid = null;
        return true;
      }

      if (!emitSingleTokens) {
        // current phrase didn't match fully: put the tokens back
        // into the unusedTokens list
        discardCharTokens( currentPhrase, unusedTokens );
        currentPhrase.setLength( 0 );
        currentSetToCheck = null;

        if (unusedTokens.size() > 0) {
          Token aToken = unusedTokens.remove( 0 );
          Log.debug( "emitting putback token 4" );
          emit( aToken );
          return true;
        }
      }
      currentSetToCheck = null;

      Log.debug( "returning at end." );
      return incrementToken( );
    }
  }

  private LinkedHashSet<String> convertToStringSet(CharArraySet charArraySet) {
    Iterator<Object> iterator = charArraySet.iterator();
    LinkedHashSet<String> strings = new LinkedHashSet<>(charArraySet.size());
    while (iterator.hasNext())  {
      char[] c = (char[]) iterator.next();
      strings.add(new String(c));
    }
    return strings;
  }

  private String nextToken( ) throws IOException {
    if (input.incrementToken( )) {
      CharTermAttribute termAttr = getTermAttribute( );
      if (termAttr != null) {
        char[] termBuf = termAttr.buffer();
        return new String(termBuf, 0, termAttr.length());
      }
    }

    return null;
  }

  private boolean isPhrase( char[] phrase ) {
    return phraseMap != null && phraseMap.containsKey(phrase, 0, phrase.length);
  }

  private boolean startsWith(CharSequence buffer, CharSequence phrase)    {
    if (phrase.length() > buffer.length()) return false;
    for (int i = 0; i < phrase.length(); i++){
      if (buffer.charAt(i) != phrase.charAt(i)) return false;
    }
    return true;
  }

  private boolean equalsWithFixedWhiteSpace(CharSequence first, CharSequence second) {
    if (first.length() != second.length()) return false;
    for (int i = 0; i < second.length(); i++) {
      if (replaceWhitespaceWith == null)  {
        if (first.charAt(i) != second.charAt(i)) {
          return false;
        }
      } else if (first.charAt(i) == second.charAt(i) || (first.charAt(i) == replaceWhitespaceWith && Character.isWhitespace(second.charAt(i)))) {
        return false;
      }

    }
    return true;
  }

  private boolean endsWith(CharSequence buffer, CharSequence phrase)    {
    if (buffer == null || phrase == null) return false;
    if (phrase.length() >= buffer.length())   return false;

    for (int i=1; i<phrase.length(); i++)   {
      if (buffer.charAt(buffer.length() - i) != phrase.charAt(phrase.length() - i))    return false;
    }
    return true;
  }

  private StringBuilder getCurrentBuffer()    {
    return getCurrentBuffer(null);
  }

  private StringBuilder getCurrentBuffer( CharSequence newToken ) {
    if (currentPhrase == null) currentPhrase = new StringBuilder( );
    if (newToken != null && newToken.length() > 0) {
      if (currentPhrase.length() > 0) currentPhrase.append( ' ' );
      currentPhrase.append( newToken );
    }

    return currentPhrase;
  }

  private String getFirst( LinkedHashSet<String> charSet ) {
    if (charSet.isEmpty()) return null;
    return charSet.iterator().next();
  }

  // avoids copying as much as possible
  private void emit(CharSequence token)    {
    CharTermAttribute termAttr = getTermAttribute( );
    termAttr.setEmpty();
    for (int i = 0; i < token.length(); i++) {
      char c = token.charAt(i);
      if (replaceWhitespaceWith != null && c == ' ')  {
        termAttr.append(replaceWhitespaceWith);
      } else  {
        termAttr.append(c);
      }
    }
    OffsetAttribute offAttr = getOffsetAttribute( );
    if (offAttr != null && offAttr.endOffset() >= token.length()){
      int start = offAttr.endOffset() - token.length();
      offAttr.setOffset( start, offAttr.endOffset());
    }

    PositionIncrementAttribute pia = getPositionIncrementAttribute( );
    if (pia != null) {
      pia.setPositionIncrement( ++positionIncr );
    }

    lastEmitted = new String(termAttr.buffer(), 0, termAttr.length());
  }

  private void emit( char[] token ) {
    System.out.println( "emit: " + new String( token ) );
    if (replaceWhitespaceWith != null) {
      token = replaceWhiteSpace( token );
    }
    CharTermAttribute termAttr = getTermAttribute( );
    termAttr.setEmpty( );
    termAttr.append( new StringBuilder( ).append( token ) );

    OffsetAttribute offAttr = getOffsetAttribute( );
    if (offAttr != null && offAttr.endOffset() >= token.length){
      int start = offAttr.endOffset() - token.length;
      offAttr.setOffset( start, offAttr.endOffset());
    }

    PositionIncrementAttribute pia = getPositionIncrementAttribute( );
    if (pia != null) {
      pia.setPositionIncrement( ++positionIncr );
    }

    lastEmitted = new String(termAttr.buffer(), 0, termAttr.length());
  }

  private void emit( Token token ) {
    emit( token.tok );
    OffsetAttribute offAttr = getOffsetAttribute( );
    if (token.endPos > token.startPos && token.startPos >= 0) {
      offAttr.setOffset( token.startPos, token.endPos );
    }
  }

  // replaces whitespace char with replaceWhitespaceWith
  private char[] replaceWhiteSpace( char[] token ) {
    char[] replaced = new char[ token.length ];
    for (int i = 0; i < token.length; i++ ) {
      if (token[i] == ' ' ) {
        replaced[i] = replaceWhitespaceWith.charValue();
      }
      else {
        replaced[i] = token[i];
      }
    }
    return replaced;
  }

  private CharTermAttribute getTermAttribute( ) {
    return getAttribute(CharTermAttribute.class);
  }

  private OffsetAttribute getOffsetAttribute( ) {
    return getAttribute(OffsetAttribute.class);
  }

  private PositionIncrementAttribute getPositionIncrementAttribute( ) {
    return getAttribute(PositionIncrementAttribute.class);
  }


  static CharArrayMap convertPhraseSet( CharArraySet phraseSet ) {
    CharArrayMap<CharArraySet> phraseMap = new CharArrayMap( 100, false);
    Iterator<Object> phraseIt = phraseSet.iterator( );
    while (phraseIt.hasNext() ) {
      char[] phrase = (char[])phraseIt.next();

      Log.debug( "'" + new String( phrase ) + "'" );

      char[] firstTerm = getFirstTerm( phrase );
      Log.debug( "'" + new String( firstTerm ) + "'" );

      CharArraySet itsPhrases = phraseMap.get( firstTerm, 0, firstTerm.length );
      if (itsPhrases == null) {
        itsPhrases = new CharArraySet( 5, false );
        phraseMap.put( new String( firstTerm ), itsPhrases );
      }

      itsPhrases.add( phrase );
    }

    return phraseMap;
  }

  private static char[] getFirstTerm( char[] phrase ) {
    int spNdx = 0;
    while ( spNdx < phrase.length ) {
      if (Character.isWhitespace( phrase[ spNdx++ ])) {
        break;
      }
    }

    char[] firstCh = new char[ spNdx-1 ];
    System.arraycopy( phrase, 0, firstCh, 0, spNdx-1 );
    return firstCh;
  }

  private boolean isSpaceChar( char ch ) {
    return " \t\n\r".indexOf( ch ) >= 0;
  }

  // reconstruct the unused tokens from the phrase (since it didn't match)
  // need to recompute the token positions based on the length of the currentPhrase,
  // the current ending position and the length of each token.
  private void discardCharTokens( StringBuilder phrase, LinkedList<Token> tokenList ) {
    Log.debug( "discardCharTokens: '" + phrase.toString() + "'" );
    OffsetAttribute offAttr = getOffsetAttribute( );
    int endPos = offAttr.endOffset( );
    int startPos = endPos - phrase.length();

    int lastSp = 0;
    for (int i = 0; i < phrase.length(); i++ ) {
      char chAt = phrase.charAt( i );
      if (Character.isWhitespace( chAt ) && i > lastSp) {
        char[] tok = new char[ i - lastSp ];
        phrase.getChars( lastSp, i, tok, 0 );
        String tokStr = new String(tok);
        if (lastEmitted == null || !endsWith( lastEmitted, tokStr )) {
          Token token = new Token( );
          token.tok = tok;

          token.startPos = startPos + lastSp;
          token.endPos = token.startPos + tok.length;
          Log.debug( "discard " + tokStr + ": " + token.startPos + ", " + token.endPos );
          tokenList.add( token );
        }
        lastSp = i+1;
      }
    }
    char[] tok = new char[ phrase.length() - lastSp ];
    phrase.getChars( lastSp, phrase.length(), tok, 0 );

    Token token = new Token( );
    token.tok = tok;
    token.endPos = endPos;
    token.startPos = endPos - tok.length;
    tokenList.add( token );
  }

  private LinkedHashSet<String> remove( LinkedHashSet<String> fromSet, String string ) {
    Log.debug( "remove from: " + string);
    Iterator<String> phraseIt = fromSet.iterator();
    while (phraseIt.hasNext() ) {
      String phrase = phraseIt.next();

      // if (!equals( phrase, charArray) && (startsWith( charArray, phrase ) || endsWith( charArray, phrase))) {
      if (!phrase.equals(string) && startsWith( phrase, string) || endsWith( string, phrase)) {
        continue;
      } else  {
        Log.debug( "removing " + phrase);
        phraseIt.remove( );
      }
    }

    return fromSet;
  }

  class Token {
    char[] tok;
    int startPos;
    int endPos;
  }
}
