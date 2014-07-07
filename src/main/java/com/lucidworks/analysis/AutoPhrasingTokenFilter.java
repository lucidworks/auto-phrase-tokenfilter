package com.lucidworks.analysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
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
  private CharArraySet currentSetToCheck = null;
	
  // The current phrase that has been seen in the token stream
  // since the first term match was encountered
  private StringBuffer currentPhrase = new StringBuffer( );
	
  // Queue to allow old tokens that ultimately did not match to be
  // emitted before new tokens are emitted so that the filter can
  // work 'transparently'
  private ArrayList<Token> unusedTokens = new ArrayList<Token>( );
  
  // If true - emit single tokens as well as auto-phrases
  private boolean emitSingleTokens;
  
  private char[] lastToken = null;
  private char[] lastEmitted = null;
  private char[] lastValid = null;

  private Character replaceWhitespaceWith = null;
	
  public AutoPhrasingTokenFilter( Version matchVersion, TokenStream input, CharArraySet phraseSet, boolean emitSingleTokens ) {
    super(input);
    	
    // Convert to CharArrayMap by iterating the char[] strings and
    // putting them into the CharArrayMap with Integer of the number
    // of tokens in the map: need this to determine when a phrase match is completed.
    this.phraseMap = convertPhraseSet( phraseSet );
    this.emitSingleTokens = emitSingleTokens;
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
	super.reset();
  }
  
  @Override
  public boolean incrementToken() throws IOException {
    if (!emitSingleTokens && unusedTokens.size() > 0) {
      Log.debug( "emitting unused phrases" );
      // emit these until the queue is empty before emitting any new stuff
      Token aToken = unusedTokens.remove( 0 );
      emit( aToken );
      return true;
    }
    
    if (lastToken != null) {
      emit( lastToken );
      lastToken = null;
      return true;
    }
    
    char[] nextToken = nextToken( );
    if (nextToken == null) {
      if (lastEmitted == null && (currentPhrase != null && currentPhrase.length() > 0)) {
        char[] lastTok = getCurrentBuffer( new char[0] );
        if (currentSetToCheck.contains( lastTok, 0, lastTok.length )) {
          emit( lastTok );
          currentPhrase.setLength( 0 );
          return true;
        }
        else if (lastValid != null) {
        	emit( lastValid );
        	return true;
        }
    	else if (!emitSingleTokens) {
          discardCharTokens( currentPhrase, unusedTokens );
          currentSetToCheck = null;
          currentPhrase.setLength( 0 );
          if (unusedTokens.size() > 0) {
            Token aToken = unusedTokens.remove( 0 );
            Log.debug( "emitting putback token");
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
   
    if (currentSetToCheck == null || currentSetToCheck.size() == 0 ) {
      Log.debug( "Checking for phrase start on '" + new String( nextToken ) + "'" );
        	
      if (phraseMap.keySet().contains( nextToken, 0, nextToken.length )) {
        // get the phrase set for this token, add it to currentSetTocheck
        currentSetToCheck = phraseMap.get(nextToken, 0, nextToken.length );
        if (currentPhrase == null) currentPhrase = new StringBuffer( );
        else currentPhrase.setLength( 0 );
        currentPhrase.append( nextToken );
        return incrementToken( );
      }
      else {
        emit( nextToken );
        // clear lastToken
        lastToken = null;
        return true;
      }
    }
    else {
      // add token to the current string buffer.
      char[] currentBuffer = getCurrentBuffer( nextToken );
        	
      if (currentSetToCheck.contains( currentBuffer, 0, currentBuffer.length )) {
        // if its the only one valid, emit it
    	// if there is a longer one, wait to see if it will be matched
    	// if the longer one breaks on the next token, emit this one...
        // emit the current phrase
        currentSetToCheck = remove( currentSetToCheck, currentBuffer );
          
    	if (currentSetToCheck.size() == 0) {
          emit( currentBuffer );
          lastValid = null;
    	}
    	else {
    	  if (emitSingleTokens) {
    		  lastToken = currentBuffer;
    		  return true;
    	  }
    	  lastValid = currentBuffer;
    	}

        if (currentSetToCheck.size() == 0 && phraseMap.keySet().contains( nextToken, 0, nextToken.length )) {
          // get the phrase set for this token, add it to currentPhrasesTocheck
          currentSetToCheck = phraseMap.get(nextToken, 0, nextToken.length );
          if (currentPhrase == null) currentPhrase = new StringBuffer( );
          else currentPhrase.setLength( 0 );
          currentPhrase.append( nextToken );
        }
        return (lastValid != null) ? incrementToken() : true;
      }
        	
      // for each phrase in currentSetToCheck - 
      // if there is a phrase prefix match, get the next token recursively
      Iterator<Object> phraseIt = currentSetToCheck.iterator();
      while (phraseIt != null && phraseIt.hasNext() ) {
        char[] phrase = (char[])phraseIt.next();
        		
        if (startsWith( phrase, currentBuffer )) {
          return incrementToken( );	
        }
      }
      
      if (lastValid != null) {
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
          Log.debug( "emitting putback token");
          emit( aToken );
	      return true;
        }
      }
      currentSetToCheck = null;
      
      Log.debug( "returning at end." );
      return incrementToken( );
    }
  }

  private char[] nextToken( ) throws IOException {
    if (input.incrementToken( )) {
	  CharTermAttribute termAttr = getTermAttribute( );
	  if (termAttr != null) {
	    char[] termBuf = termAttr.buffer();
		char[] nextTok = new char[ termAttr.length() ];
		System.arraycopy( termBuf, 0, nextTok, 0, termAttr.length());
		return nextTok;
	  }
    }
		
	return null;
  }
	
  private boolean isPhrase( char[] phrase ) {
    return phraseMap != null && phraseMap.containsKey(phrase, 0, phrase.length);
  }
	
  private boolean startsWith( char[] buffer, char[] phrase ) {
    if (phrase.length >= buffer.length) return false;
    for (int i = 0; i < phrase.length; i++){
      if (buffer[i] != phrase[i]) return false;
	}
	return true;
  }
  
  private boolean equals( char[] buffer, char[] phrase ) {
    if (phrase.length != buffer.length) return false;
	for (int i = 0; i < phrase.length; i++){
	  if (buffer[i] != phrase[i]) return false;
	}
	return true;
  }
  
  private boolean endsWith( char[] buffer, char[] phrase ) {
	if (buffer == null || phrase == null) return false;
	
    if (phrase.length >= buffer.length) return false;
	for (int i = 1; i < phrase.length-1; ++i){
	  if (buffer[buffer.length - i] != phrase[phrase.length - i]) return false;
	}		
	return true;
  }
	
  private char[] getCurrentBuffer( char[] newToken ) {
    if (currentPhrase == null) currentPhrase = new StringBuffer( );
    if (newToken != null && newToken.length > 0) {
      if (currentPhrase.length() > 0) currentPhrase.append( ' ' );
      currentPhrase.append( newToken );
    }
    
    char[] currentBuff = new char[ currentPhrase.length() ];
    currentPhrase.getChars( 0,  currentPhrase.length( ), currentBuff, 0 );
    return currentBuff;
  }
	
	
  private void emit( char[] token ) {
	Log.debug( "emit: " + new String( token ) );
	if (replaceWhitespaceWith != null) {
		token = replaceWhiteSpace( token );
	}
	CharTermAttribute termAttr = getTermAttribute( );
	termAttr.setEmpty( );
	termAttr.append( new StringBuilder( ).append( token ) );
	
	OffsetAttribute offAttr = getOffsetAttribute( );
	if (offAttr.endOffset() >= token.length){ 
	  int start = offAttr.endOffset() - token.length;
	  offAttr.setOffset( start, offAttr.endOffset());
	}
	
	lastEmitted = token;
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
    // get char term attr from current state
    Iterator<AttributeImpl> attrIt = getAttributeImplsIterator();
    while (attrIt != null && attrIt.hasNext() ) {
      AttributeImpl attrImp = attrIt.next();
      if (attrImp instanceof CharTermAttribute) {
        return (CharTermAttribute)attrImp;
	  }
    }
		
    return null;
  }
 
  private OffsetAttribute getOffsetAttribute( ) {
    // get char term attr from current state
	Iterator<AttributeImpl> attrIt = getAttributeImplsIterator();
	while (attrIt != null && attrIt.hasNext() ) {
	  AttributeImpl attrImp = attrIt.next();
	  if (attrImp instanceof OffsetAttribute) {
	    return (OffsetAttribute)attrImp;
      }
	}
			
	return null;
  }
  
	
  private CharArrayMap convertPhraseSet( CharArraySet phraseSet ) {
	CharArrayMap<CharArraySet> phraseMap = new CharArrayMap( Version.LUCENE_46, 100, false);
	Iterator<Object> phraseIt = phraseSet.iterator( ); 
	while (phraseIt != null && phraseIt.hasNext() ) {
	  char[] phrase = (char[])phraseIt.next();

	  Log.debug( "'" + new String( phrase ) + "'" );
			
	  char[] firstTerm = getFirstTerm( phrase );
	  Log.debug( "'" + new String( firstTerm ) + "'" );
			
	  CharArraySet itsPhrases = phraseMap.get( firstTerm, 0, firstTerm.length );
	  if (itsPhrases == null) {
	    itsPhrases = new CharArraySet( Version.LUCENE_46, 5, false );
		phraseMap.put( new String( firstTerm ), itsPhrases );
      }
			
      itsPhrases.add( phrase );
	}
	
	return phraseMap;
  }
	
  private char[] getFirstTerm( char[] phrase ) {
    int spNdx = 0;
	while ( spNdx < phrase.length ) {
	  if (isSpaceChar( phrase[ spNdx++ ])) {
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
  private void discardCharTokens( StringBuffer phrase, ArrayList<Token> tokenList ) {
	Log.debug( "discardCharTokens: '" + phrase.toString() + "'" );
	OffsetAttribute offAttr = getOffsetAttribute( );
	int endPos = offAttr.endOffset( );
	int startPos = endPos - phrase.length();
	
    int lastSp = 0;
	for (int i = 0; i < phrase.length(); i++ ) {
	  char chAt = phrase.charAt( i );
	  if (isSpaceChar( chAt ) && i > lastSp) {
	    char[] tok = new char[ i - lastSp ];
		phrase.getChars( lastSp, i, tok, 0 );
		if (lastEmitted == null || !endsWith( lastEmitted, tok )) {
		  Token token = new Token( );
		  token.tok = tok;
		  
		  token.startPos = startPos + lastSp;
		  token.endPos = token.startPos + tok.length;
		  Log.debug( "discard " + new String( tok ) + ": " + token.startPos + ", " + token.endPos );
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
  
  private CharArraySet remove( CharArraySet fromSet, char[] charArray ) {
	  CharArraySet newSet = new CharArraySet( Version.LUCENE_46, 5, false );
	  Iterator<Object> phraseIt = currentSetToCheck.iterator();
      while (phraseIt != null && phraseIt.hasNext() ) {
        char[] phrase = (char[])phraseIt.next();
        		
        if (!equals( phrase, charArray) && startsWith( phrase, charArray )) {
        	newSet.add( phrase );
        }
      }
      
	  return newSet;
  }
  
  class Token {
	  char[] tok;
	  int startPos;
	  int endPos;
  }
}
