/*
 *  @(#)SecurityPropertiesTest.java
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

class SecurityPropertiesTest {

    @Test
    @DisplayName("Test getters and setters of SecurityProperties")
    void gettersAndSetters() {
        var props = new SecurityProperties();
        var keystore = new StoreProperties();
        keystore.setLocation("classpath:backbone.jks");
        var truststore = new StoreProperties();
        truststore.setLocation("classpath:umdc-truststore.jks");
        var mcam = new ManagementAuthenticatorProperties();
        mcam.setKeyAlias("mcam-rsa");

        props.setKeystore(keystore);
        props.setTruststore(truststore);
        props.setManagementAuthenticator(mcam);

        Assertions.assertAll("Test Getters and Setters",
                () -> Assertions.assertEquals(keystore, props.getKeystore()),
                () -> Assertions.assertEquals(truststore, props.getTruststore()),
                () -> Assertions.assertEquals(mcam, props.getManagementAuthenticator())
        );
    }

    @Test
    @DisplayName("default constructor leaves every field null")
    void defaultConstructor() {
        var props = new SecurityProperties();

        Assertions.assertAll("Test default state",
                () -> Assertions.assertNull(props.getKeystore()),
                () -> Assertions.assertNull(props.getTruststore()),
                () -> Assertions.assertNull(props.getManagementAuthenticator())
        );
    }
}
