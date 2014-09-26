package com.lucidworks.analysis;

import junit.framework.TestCase;

public class TestCharArrayUtil extends TestCase {

    public void testStartsWithNullFirstParam() throws Exception {
        char[] phrase = "something".toCharArray();
        assertFalse(CharArrayUtil.startsWith(null, phrase));
    }

    public void testStartsWithNullSecondParam() throws Exception {
        char[] buffer = "something".toCharArray();
        assertFalse(CharArrayUtil.startsWith(buffer, null));
    }

    public void testStartsWithNullParams() throws Exception {
        assertFalse(CharArrayUtil.startsWith(null, null));
    }

    public void testStartsWithBiggerPhrase() throws Exception {
        char[] buffer = "small".toCharArray();
        char[] phrase = "much bigger".toCharArray();
        assertFalse(CharArrayUtil.startsWith(buffer, phrase));
    }

    public void testStartsWithSame() throws Exception {
        char[] buffer = "matches".toCharArray();
        char[] phrase = "matches".toCharArray();
        assertTrue(CharArrayUtil.startsWith(buffer, phrase));
    }

    public void testStartsWithEmptyStrings() throws Exception {
        char[] buffer = "".toCharArray();
        char[] phrase = "".toCharArray();
        assertTrue(CharArrayUtil.startsWith(buffer, phrase));
    }

    public void testStartsWithOneCharacterStrings() throws Exception {
        char[] buffer = "1".toCharArray();
        char[] phrase = "1".toCharArray();
        assertTrue(CharArrayUtil.startsWith(buffer, phrase));
    }

    public void testStartsWithSubstring() throws Exception {
        char[] buffer = "something".toCharArray();
        char[] phrase = "some".toCharArray();
        assertTrue(CharArrayUtil.startsWith(buffer, phrase));
    }

    public void testStartsWithNoMatchSubstring() throws Exception {
        char[] buffer = "something".toCharArray();
        char[] phrase = "diff".toCharArray();
        assertFalse(CharArrayUtil.startsWith(buffer, phrase));
    }

    public void testStartsWithNoMatchSameFirsCharacter() throws Exception {
        char[] buffer = "something".toCharArray();
        char[] phrase = "sing".toCharArray();
        assertFalse(CharArrayUtil.startsWith(buffer, phrase));
    }

    public void testEndsWithNullFirstParam() throws Exception {
        char[] phrase = "something".toCharArray();
        assertFalse(CharArrayUtil.endsWith(null, phrase));
    }

    public void testEndsWithNullSecondParam() throws Exception {
        char[] buffer = "something".toCharArray();
        assertFalse(CharArrayUtil.endsWith(buffer, null));
    }

    public void testEndsWithNullParams() throws Exception {
        assertFalse(CharArrayUtil.endsWith(null, null));
    }

    public void testEndsWithBiggerPhrase() throws Exception {
        char[] buffer = "small".toCharArray();
        char[] phrase = "much bigger".toCharArray();
        assertFalse(CharArrayUtil.endsWith(buffer, phrase));
    }

    public void testEndsWithSame() throws Exception {
        char[] buffer = "matches".toCharArray();
        char[] phrase = "matches".toCharArray();
        assertTrue(CharArrayUtil.endsWith(buffer, phrase));
    }

    public void testEndsWithEmptyStrings() throws Exception {
        char[] buffer = "".toCharArray();
        char[] phrase = "".toCharArray();
        assertTrue(CharArrayUtil.endsWith(buffer, phrase));
    }

    public void testEndsWithOneCharacterStrings() throws Exception {
        char[] buffer = "1".toCharArray();
        char[] phrase = "1".toCharArray();
        assertTrue(CharArrayUtil.endsWith(buffer, phrase));
    }

    public void testEndsWithSubstring() throws Exception {
        char[] buffer = "something".toCharArray();
        char[] phrase = "ing".toCharArray();
        assertTrue(CharArrayUtil.endsWith(buffer, phrase));
    }

    public void testEndsWithNoMatchSubstring() throws Exception {
        char[] buffer = "something".toCharArray();
        char[] phrase = "diff".toCharArray();
        assertFalse(CharArrayUtil.endsWith(buffer, phrase));
    }

    public void testEndsWithNoMatchSameLastCharacter() throws Exception {
        char[] buffer = "something".toCharArray();
        char[] phrase = "big".toCharArray();
        assertFalse(CharArrayUtil.endsWith(buffer, phrase));
    }

    public void testEqualsNull() throws Exception {
        assertFalse(CharArrayUtil.equals(null, null));
    }

    public void testEqualsDifferentLengths() throws Exception {
        char[] buffer = "something".toCharArray();
        char[] phrase = "big".toCharArray();
        assertFalse(CharArrayUtil.equals(buffer, phrase));
    }

    public void testEqualsEqualLengthsDifferentStrings() throws Exception {
        char[] buffer = "foo".toCharArray();
        char[] phrase = "bar".toCharArray();
        assertFalse(CharArrayUtil.equals(buffer, phrase));
    }

    public void testEqualsEqualStrings() throws Exception {
        char[] buffer = "same".toCharArray();
        char[] phrase = "same".toCharArray();
        assertTrue(CharArrayUtil.equals(buffer, phrase));
    }

    public void testIsSpaceSpace() throws Exception {
        assertTrue(CharArrayUtil.isSpaceChar(' '));
    }

    public void testIsSpaceTab() throws Exception {
        assertTrue(CharArrayUtil.isSpaceChar('\t'));
    }

    public void testIsSpaceReturn() throws Exception {
        assertTrue(CharArrayUtil.isSpaceChar('\r'));
    }

    public void testIsSpaceNewline() throws Exception {
        assertTrue(CharArrayUtil.isSpaceChar('\n'));
    }

    public void testIsSpaceNotSpace() throws Exception {
        assertFalse(CharArrayUtil.isSpaceChar('a'));
    }

    public void testGetFirstTermTwoTerms() throws Exception {
        char[] input = "two terms".toCharArray();
        char[] expected = "two".toCharArray();
        char[] actual = CharArrayUtil.getFirstTerm(input);
        assertEquals(new String(expected), new String(actual));
    }

    public void testGetFirstTermOneTerm() throws Exception {
        char[] input = "one".toCharArray();
        char[] expected = "one".toCharArray();
        char[] actual = CharArrayUtil.getFirstTerm(input);
        assertEquals(new String(expected), new String(actual));
    }

    public void testGetFirstTermEmpty() throws Exception {
        char[] input = "".toCharArray();
        char[] expected = "".toCharArray();
        char[] actual = CharArrayUtil.getFirstTerm(input);
        assertEquals(new String(expected), new String(actual));
    }

    public void testGetFirstTermOneLetter() throws Exception {
        char[] input = "a".toCharArray();
        char[] expected = "a".toCharArray();
        char[] actual = CharArrayUtil.getFirstTerm(input);
        assertEquals(new String(expected), new String(actual));
    }

    public void testGetFirstTermManyTerms() throws Exception {
        char[] input = "here is a big list of things because why not?".toCharArray();
        char[] expected = "here".toCharArray();
        char[] actual = CharArrayUtil.getFirstTerm(input);
        assertEquals(new String(expected), new String(actual));
    }
}