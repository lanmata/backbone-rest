/*
 * @(#)$file.className.java.
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

package com.prx.backoffice.to.feature;

import com.prx.backoffice.enums.keys.FeatureMessageKey;
import com.prx.backoffice.v1.feature.api.to.FeatureListResponse;
import com.prx.commons.pojo.Feature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;

/**
 * FeatureListResponseTest.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 26-03-2021
 */

public class FeatureListResponseTest {

    @Test
    public void gettersAndSetters() {
        final var featureListResponse = new FeatureListResponse();
        var feature = new Feature();
        feature.setId(1L);
        feature.setName("Feature");
        feature.setDescription("Descripción de feature");
        feature.setActive(true);
        featureListResponse.setList(new ArrayList<>());
        featureListResponse.getList().add(feature);
        featureListResponse.setCode(FeatureMessageKey.FEATURE_OK.getCode());
        featureListResponse.setMessage(FeatureMessageKey.FEATURE_OK.getStatus());
        featureListResponse.setDateTime(LocalDateTime.now(ZoneId.systemDefault()));

        Assertions.assertAll("Test Getters and Setters",
                () -> Assertions.assertNotNull(featureListResponse.getList()),
                () -> Assertions.assertNotNull(featureListResponse.getCode()),
                () -> Assertions.assertNotNull(featureListResponse.getDateTime()),
                () -> Assertions.assertNotNull(featureListResponse.getMessage()),
                () -> Assertions.assertNotNull(featureListResponse.toString()),
                () -> Assertions.assertNotEquals(1, featureListResponse.hashCode()),
                () -> Assertions.assertNotEquals(new FeatureListResponse(), featureListResponse)
        );
    }
}