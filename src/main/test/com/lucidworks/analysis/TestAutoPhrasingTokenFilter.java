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
  		      "income tax", "tax refund", "property tax"
  	     ), false);
   	 
       final String input = "what is my income tax refund this year now that my property tax is so high";
       WhitespaceTokenizer wt = new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader(input));
       AutoPhrasingTokenFilter aptf = new AutoPhrasingTokenFilter( TEST_VERSION_CURRENT, wt, phraseSets, false );
       aptf.setReplaceWhitespaceWith( new Character( '_' ) );
       CharTermAttribute term = aptf.addAttribute(CharTermAttribute.class);
       aptf.reset();

      // printTokens( aptf, term );
       
       assertTrue(aptf.incrementToken());
       assertEquals( "what", term.toString());
       assertTrue(aptf.incrementToken());
       assertEquals( "is", term.toString());
       assertTrue(aptf.incrementToken());
       assertEquals( "my", term.toString());
       assertTrue(aptf.incrementToken());
       assertEquals( "income_tax", term.toString());
       assertTrue(aptf.incrementToken());
       assertEquals( "tax_refund", term.toString());
       assertTrue(aptf.incrementToken());
       assertEquals( "this", term.toString());
       assertTrue(aptf.incrementToken());
       assertEquals( "year", term.toString());
       assertTrue(aptf.incrementToken());
       assertEquals( "now", term.toString());
       assertTrue(aptf.incrementToken());
       assertEquals( "that", term.toString());
       assertTrue(aptf.incrementToken());
       assertEquals( "my", term.toString());
       assertTrue(aptf.incrementToken());
       assertEquals( "property_tax", term.toString());
       assertTrue(aptf.incrementToken());
       assertEquals( "is", term.toString());
       assertTrue(aptf.incrementToken());
       assertEquals( "so", term.toString());
       assertTrue(aptf.incrementToken());
       assertEquals( "high", term.toString());
       
       System.out.println( "testAutoPhrase: OK" );
   }
    
    public void testAutoPhraseEmitSingle( ) throws Exception {
    	 final CharArraySet phraseSets = new CharArraySet(TEST_VERSION_CURRENT, Arrays.asList(
   		      "income tax", "tax refund", "property tax"
   	     ), false);
    	 
        final String input = "what is my income tax refund this year now that my property tax is so high";
        WhitespaceTokenizer wt = new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader(input));
        AutoPhrasingTokenFilter aptf = new AutoPhrasingTokenFilter( TEST_VERSION_CURRENT, wt, phraseSets, true );
        aptf.setReplaceWhitespaceWith( new Character( '_' ) );
        CharTermAttribute term = aptf.addAttribute(CharTermAttribute.class);
        aptf.reset();

        // printTokens( aptf, term );
        
        assertTrue(aptf.incrementToken());
        assertEquals( "what", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals( "is", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals( "my", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals( "income", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals( "income_tax", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals( "tax", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals( "tax_refund", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals( "refund", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals( "this", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals( "year", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals( "now", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals( "that", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals( "my", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals( "property", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals( "property_tax", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals( "tax", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals( "is", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals( "so", term.toString());
        assertTrue(aptf.incrementToken());
        assertEquals( "high", term.toString());
        
        System.out.println( "testAutoPhraseEmitSingle: OK" );
    }
    
    public void testOverlappingAtBeginning( ) throws Exception {
      	 final CharArraySet phraseSets = new CharArraySet(TEST_VERSION_CURRENT, Arrays.asList(
     		      "new york", "new york city", "city of new york"
     	     ), false);
      	 
      	  final String input = "new york city is great";
          WhitespaceTokenizer wt = new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader(input));
          AutoPhrasingTokenFilter aptf = new AutoPhrasingTokenFilter( TEST_VERSION_CURRENT, wt, phraseSets, false );
          aptf.setReplaceWhitespaceWith( new Character( '_' ) );
          CharTermAttribute term = aptf.addAttribute(CharTermAttribute.class);
          aptf.reset();

         // printTokens( aptf, term );
          
          assertTrue(aptf.incrementToken());
          assertEquals( "new_york_city", term.toString());
          assertTrue(aptf.incrementToken());
          assertEquals( "is", term.toString());
          assertTrue(aptf.incrementToken());
          assertEquals( "great", term.toString());
          
          System.out.println( "testOverlappingAtBeginning: OK" );
      }
    
    public void testOverlappingAtBeginningEmitSingle( ) throws Exception {
   	 final CharArraySet phraseSets = new CharArraySet(TEST_VERSION_CURRENT, Arrays.asList(
  		      "new york", "new york city", "city of new york"
  	     ), false);
   	 

   	   final String input = "new york city is great";
       WhitespaceTokenizer wt = new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader(input));
       AutoPhrasingTokenFilter aptf = new AutoPhrasingTokenFilter( TEST_VERSION_CURRENT, wt, phraseSets, true );
       aptf.setReplaceWhitespaceWith( new Character( '_' ) );
       CharTermAttribute term = aptf.addAttribute(CharTermAttribute.class);
       aptf.reset();

       // printTokens( aptf, term );
       
       assertTrue(aptf.incrementToken());
       assertEquals( "new", term.toString());
       assertTrue(aptf.incrementToken());
       assertEquals( "york", term.toString());
       assertTrue(aptf.incrementToken());
       assertEquals( "new_york", term.toString());
       assertTrue(aptf.incrementToken());
       assertEquals( "new_york_city", term.toString());
       assertTrue(aptf.incrementToken());
       assertEquals( "city", term.toString());
       assertTrue(aptf.incrementToken());
       assertEquals( "is", term.toString());
       assertTrue(aptf.incrementToken());
       assertEquals( "great", term.toString());
       
       System.out.println( "testOverlappingAtBeginningEmitSingle: OK" );
   }
    
   public void testOverlappingAtEndEmitSingle( ) throws Exception {
     final CharArraySet phraseSets = new CharArraySet(TEST_VERSION_CURRENT, Arrays.asList(
    		      "new york", "new york city", "city of new york"
    	     ), false);
     	 
     final String input = "the great city of new york";
     
     WhitespaceTokenizer wt = new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader(input));
     AutoPhrasingTokenFilter aptf = new AutoPhrasingTokenFilter( TEST_VERSION_CURRENT, wt, phraseSets, true );
     aptf.setReplaceWhitespaceWith( new Character( '_' ) );
     CharTermAttribute term = aptf.addAttribute(CharTermAttribute.class);
     aptf.reset();

     // printTokens( aptf, term );
     
     assertTrue(aptf.incrementToken());
     assertEquals( "the", term.toString());
     assertTrue(aptf.incrementToken());
     assertEquals( "great", term.toString());
     assertTrue(aptf.incrementToken());
     assertEquals( "city", term.toString());
     assertTrue(aptf.incrementToken());
     assertEquals( "of", term.toString());
     assertTrue(aptf.incrementToken());
     assertEquals( "new", term.toString());
     assertTrue(aptf.incrementToken());
     assertEquals( "york", term.toString());
     assertTrue(aptf.incrementToken());
     assertEquals( "city_of_new_york", term.toString());
     assertTrue(aptf.incrementToken());
     assertEquals( "new_york", term.toString());

     System.out.println( "testOverlappingAtEndEmitSingle: OK" );
   }
   
   public void testOverlappingAtEnd( ) throws Exception {
	     final CharArraySet phraseSets = new CharArraySet(TEST_VERSION_CURRENT, Arrays.asList(
   		      "new york", "new york city", "city of new york"
   	     ), false);
    	 
    final String input = "the great city of new york";
    
    WhitespaceTokenizer wt = new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader(input));
    AutoPhrasingTokenFilter aptf = new AutoPhrasingTokenFilter( TEST_VERSION_CURRENT, wt, phraseSets, false );
    aptf.setReplaceWhitespaceWith( new Character( '_' ) );
    CharTermAttribute term = aptf.addAttribute(CharTermAttribute.class);
    aptf.reset();

    // printTokens( aptf, term );
    
    assertTrue(aptf.incrementToken());
    assertEquals( "the", term.toString());
    assertTrue(aptf.incrementToken());
    assertEquals( "great", term.toString());
    assertTrue(aptf.incrementToken());
    assertEquals( "city_of_new_york", term.toString());
    
    System.out.println( "testOverlappingAtEnd: OK" );
   }
   
   public void testIncompletePhrase( ) throws Exception {
	     final CharArraySet phraseSets = new CharArraySet(TEST_VERSION_CURRENT, Arrays.asList(
	    	"big apple", "new york city", "property tax", "three word phrase"
 	     ), false);
  	 
    final String input = "some new york";
  
    WhitespaceTokenizer wt = new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader(input));
    AutoPhrasingTokenFilter aptf = new AutoPhrasingTokenFilter( TEST_VERSION_CURRENT, wt, phraseSets, false );
    aptf.setReplaceWhitespaceWith( new Character( '_' ) );
    CharTermAttribute term = aptf.addAttribute(CharTermAttribute.class);
    aptf.reset();

    printTokens( aptf, term );
    
    /*assertTrue(aptf.incrementToken());
    assertEquals( "some", term.toString());
    assertTrue(aptf.incrementToken());
    assertEquals( "new_york", term.toString());
    assertTrue(aptf.incrementToken());
    assertEquals( "york", term.toString());*/
    
  
    System.out.println( "testIncompletePhrase: OK" );
 }
	
   private void printTokens( AutoPhrasingTokenFilter aptf, CharTermAttribute term ) throws Exception {
       boolean hasToken = false;
       do {
       	hasToken = aptf.incrementToken( );
       	if (hasToken) System.out.println( "token:'" + term.toString( ) + "'" );
       } while (hasToken);
   }
   
    public static void main( String[] args ) {
    	TestAutoPhrasingTokenFilter aptft = new TestAutoPhrasingTokenFilter( );
    	try {
    	    aptft.testAutoPhraseEmitSingle( );
    	    aptft.testAutoPhrase( );
    	    aptft.testOverlappingAtBeginningEmitSingle();
    	    aptft.testOverlappingAtBeginning();
    	    aptft.testOverlappingAtEndEmitSingle();
    	    aptft.testOverlappingAtEnd();
    	    
    	    aptft.testIncompletePhrase();
    	}
    	catch ( Exception e ) {
    		System.out.println( "got Exception: " + e );
    	}
    }
}
