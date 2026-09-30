/*
 *  @(#)StorePropertiesTest.java
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

package com.umdc.backoffice.property;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StorePropertiesTest {

    @Test
    @DisplayName("Test getters and setters of StoreProperties")
    void gettersAndSetters() {
        var store = new StoreProperties();
        var expectedPassword = System.getProperty("test.store.password", "");
        store.setLocation("classpath:backbone.jks");
        store.setPassword(expectedPassword);
        store.setType("JKS");

        Assertions.assertAll("Test Getters and Setters",
                () -> Assertions.assertEquals("classpath:backbone.jks", store.getLocation()),
                () -> Assertions.assertEquals(expectedPassword, store.getPassword()),
                () -> Assertions.assertEquals("JKS", store.getType())
        );
    }

    @Test
    @DisplayName("default constructor leaves every field null")
    void defaultConstructor() {
        var store = new StoreProperties();

        Assertions.assertAll("Test default state",
                () -> Assertions.assertNull(store.getLocation()),
                () -> Assertions.assertNull(store.getPassword()),
                () -> Assertions.assertNull(store.getType())
        );
    }
}
