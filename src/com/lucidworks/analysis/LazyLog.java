package com.lucidworks.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Only calls logDebug is debug logging is enabled to avoid char[] to String casts and String.formats.
 * It exposes overloads instead of a single method with Object... args because char[].toString()
 * doesn't get the string. A new string must be created.
 */
public class LazyLog {
    private static final Logger Log = LoggerFactory.getLogger(AutoPhrasingTokenFilter.class);

    public static void logDebug (String format) {
        Log.debug(format);
    }

    public static void logDebug (String format, char[] arg) {
        if (Log.isDebugEnabled()) {
            Log.debug(String.format(format, arg == null ? "NULL" : new String(arg)));
        }
    }

    public static void logDebug (String format, int arg) {
        if (Log.isDebugEnabled()) {
            Log.debug(String.format(format, arg));
        }
    }

    public static void logDebug (String format, char[] arg0, int arg1, int arg2) {
        if (Log.isDebugEnabled()) {
            Log.debug(String.format(format, new String(arg0), arg1, arg2));
        }
    }

    public static void logDebug (String format, StringBuffer arg) {
        if (Log.isDebugEnabled()) {
            Log.debug(String.format(format, arg));
        }
    }
}
