/*
 * @(#)LogActionKeyTest.java.
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

package com.prx.backoffice.enums.keys;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LogActionKeyTest.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 26-03-2021
 */

public class LogActionKeyTest {

    @Test
    void keys() {
        for (final var tp : LogActionKey.values()) {
            Assertions.assertTrue(Arrays.asList(LogActionKey.values()).contains(tp));
            Assertions.assertNotEquals("0", tp.getValue());
        }
    }

}