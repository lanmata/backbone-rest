/*
 *  @(#)RoleFeatureMessageKeyTest.java
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

/**
 * RolFeatureMessageKeyTest.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 18-02-2021
 */
public class RoleFeatureMessageKeyTest {

    @Test
    void keys() {
        for (final var tp : RoleFeatureMessageKey.values()) {
            Assertions.assertTrue(Arrays.asList(RoleFeatureMessageKey.values()).contains(tp));
            Assertions.assertNotEquals(0, tp.getCode());
            Assertions.assertNotNull(tp.getStatus());
        }
    }

}
