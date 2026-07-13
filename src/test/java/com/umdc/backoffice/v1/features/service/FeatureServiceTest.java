/*
 *  @(#)FeatureServiceTest.java
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

package com.umdc.backoffice.v1.features.service;

import com.umdc.commons.general.pojo.Feature;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FeatureServiceTest {

    private final FeatureService featureService = new FeatureService() {
    };

    @Test
    @DisplayName("Test creating a feature")
    void create() {
        Feature feature = new Feature();
        assertThrows(NotImplementedException.class, () -> featureService.create(feature));
    }

    @Test
    @DisplayName("Test listing features with UUIDs")
    void list() {
        List<String> ids = List.of(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        assertThrows(NotImplementedException.class, () -> featureService.list(ids, true));
    }

    @Test
    @DisplayName("Test updating a feature")
    void update() {
        UUID id = UUID.randomUUID();
        Feature feature = new Feature();
        assertThrows(NotImplementedException.class, () -> featureService.update(id, feature));
    }

    @Test
    @DisplayName("Test deleting a feature")
    void delete() {
        UUID id = UUID.randomUUID();
        Feature feature = new Feature();
        assertThrows(NotImplementedException.class, () -> featureService.delete(id, feature));
    }

    @Test
    @DisplayName("Test listing features with IDs")
    void testList() {
        UUID appId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        assertThrows(NotImplementedException.class, () -> featureService.list(appId, roleId, userId));
    }
}
