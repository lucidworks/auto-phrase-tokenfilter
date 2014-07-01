package com.lucidworks.analysis;

import java.util.Arrays;
import java.io.StringReader;

import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.util.CharArraySet;

public class TestAutoPhrasingTokenFilter extends BaseTokenStreamTestCase {
    
    public void testAutoPhrase( ) throws Exception {
    	 final CharArraySet phraseSets = new CharArraySet(TEST_VERSION_CURRENT, Arrays.asList(
   		      "income tax", "tax refund", "property tax", "rear tow bar"
   	     ), false);
    	 
        // final String input = "what is income tax refund this year now that property tax is high";
    	final String input = "rear bar";
        WhitespaceTokenizer wt = new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader(input));
        AutoPhrasingTokenFilter aptf = new AutoPhrasingTokenFilter( TEST_VERSION_CURRENT, wt, phraseSets, false );
        CharTermAttribute term = aptf.addAttribute(CharTermAttribute.class);
        aptf.reset();

        boolean hasToken = false;
        do {
        	hasToken = aptf.incrementToken( );
        	if (hasToken) System.out.println( "token:'" + term.toString( ) + "'" );
        } while (hasToken);

        
        /*assertTrue(aptf.incrementToken());
        assertEquals( "what", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals( "is", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals( "income tax", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals( "tax refund", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals( "this", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals( "year", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals( "now", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals( "that", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals( "property tax", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals( "is", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals( "high", term.toString());
        */

    }
	  
    public static void main( String[] args ) {
    	TestAutoPhrasingTokenFilter aptft = new TestAutoPhrasingTokenFilter( );
    	try {
    	    aptft.testAutoPhrase( );
    	}
    	catch ( Exception e ) {
    		System.out.println( "got Exception: " + e );
    	}
    }
}
