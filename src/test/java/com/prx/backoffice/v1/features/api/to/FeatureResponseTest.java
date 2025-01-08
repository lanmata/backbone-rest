/*
 *  @(#)FeatureResponseTest.java
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

package com.prx.backoffice.v1.features.api.to;

import com.prx.backoffice.constant.keys.FeatureMessageKey;
import com.prx.commons.general.pojo.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * FeatureResponseTest.
 *
 * @version 1.0.0, 14-02-2021
 */

public class FeatureResponseTest {

    @Test
    @DisplayName("Test getters and setters of FeatureResponse")
    public void gettersAndSetters() {
        final var featureResponse = new FeatureResponse();
        final var feature = new Feature();
        feature.setId(UUID.randomUUID());
        feature.setActive(true);
        feature.setDescription("Feature description");
        feature.setName("Feature name");
        featureResponse.setFeature(feature);
        featureResponse.setCode(FeatureMessageKey.FEATURE_OK.getCode());
        featureResponse.setMessage(FeatureMessageKey.FEATURE_OK.getStatus());
        featureResponse.setDateTime(LocalDateTime.now(ZoneId.systemDefault()));

        assertAll(() -> assertNotNull(featureResponse.getFeature()),
                () -> assertNotNull(featureResponse.getCode()),
                () -> assertNotNull(featureResponse.getDateTime()),
                () -> assertNotNull(featureResponse.getMessage()),
                () -> assertNotNull(featureResponse.toString())
        );
    }
}
