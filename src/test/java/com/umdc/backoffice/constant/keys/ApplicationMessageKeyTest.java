/*
 *  @(#)ApplicationMessageKeyTest.java
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
package com.umdc.backoffice.constant.keys;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class ApplicationMessageKeyTest {

    @Test
    void keys() {
        for (final var key : ApplicationMessageKey.values()) {
            Assertions.assertTrue(Arrays.asList(ApplicationMessageKey.values()).contains(key));
            Assertions.assertNotEquals(0, key.getCode());
            Assertions.assertNotNull(key.getStatus());
        }
    }
}
