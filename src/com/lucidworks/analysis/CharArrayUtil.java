package com.lucidworks.analysis;

/**
 * Utility functions for working with character arrays.
 */
public class CharArrayUtil {
    public static boolean equals(char[] buffer, char[] phrase) {
        if (buffer == null || phrase == null) return false;

        if (phrase.length != buffer.length) return false;
        for (int i = 0; i < phrase.length; i++) {
            if (buffer[i] != phrase[i]) return false;
        }
        return true;
    }

    public static boolean startsWith(char[] buffer, char[] phrase) {
        if (buffer == null || phrase == null) return false;

        if (phrase.length > buffer.length) return false;
        for (int i = 0; i < phrase.length; i++) {
            if (buffer[i] != phrase[i]) return false;
        }
        return true;
    }

    public static boolean endsWith(char[] buffer, char[] phrase) {
        if (buffer == null || phrase == null) return false;

        if (phrase.length > buffer.length) return false;
        for (int i = 1; i < phrase.length + 1; ++i) {
            if (buffer[buffer.length - i] != phrase[phrase.length - i]) return false;
        }
        return true;
    }
}
