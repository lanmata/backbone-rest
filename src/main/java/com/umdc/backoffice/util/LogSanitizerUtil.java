/*
 *  @(#)LogSanitizerUtil.java
 *
 *  Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *
 *   All rights to this product are owned by Luis Antonio Mata Mata and may only
 *  be used under the terms of its associated license document. You may NOT
 *  copy, modify, sublicense, or distribute this source file or portions of
 *  it unless previously authorized in writing by Luis Antonio Mata Mata.
 *  In any event, this notice and the above copyright must always be included
 *  verbatim with this file.
 */
package com.umdc.backoffice.util;

/**
 * Neutralizes CRLF/control characters in values that originate from user input before
 * they are written to the log (CWE-117 log injection / log forging — a caller could
 * otherwise embed a newline plus a fabricated log line to spoof audit trail entries).
 */
public final class LogSanitizerUtil {

    private LogSanitizerUtil() {
    }

    /**
     * @param value a value to interpolate into a log message; may be {@code null}
     * @return {@code value} with CR, LF and tab characters replaced by {@code '_'}, or
     *         {@code null} when {@code value} is {@code null}
     */
    public static String sanitize(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString().replaceAll("[\r\n\t]", "_");
    }
}
