/*
 * @(#)LogActionKey.java.
 *
 * Copyright (c) Luis Antonio Mata Mata. All rights reserved.
 *
 * All rights to this product are owned by Luis Antonio Mata Mata and may only
 * be used under the terms of its associated license document. You may NOT
 * copy, modify, sublicense, or distribute this source file or portions of
 * it unless previously authorized in writing by Luis Antonio Mata Mata.
 * In any event, this notice and the above copyright must always be included
 * verbatim with this file.
 */

package com.prx.backoffice.constant.keys;

/**
 * Enum representing various log action keys used in the application.
 * Provides a value for each log action key.
 *
 * @version 1.0.0, 22-03-2021
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 */
public enum LogActionKey {
    /**
     * Represents an empty log action key.
     */
    EMPTY("");

    private final String value;

    /**
     * Constructor for LogActionKey enum.
     *
     * @param value the value of the log action key
     */
    LogActionKey(String value){
        this.value = value;
    }

    /**
     * Gets the value of the log action key.
     *
     * @return the value of the log action key
     */
    public String getValue() {
        return value;
    }
}
