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

package com.prx.backoffice.v1.features.service;

import com.prx.commons.general.pojo.Feature;
import org.apache.commons.lang.NotImplementedException;
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
        assertThrows(NotImplementedException.class, () -> featureService.create(new Feature()));
    }

    @Test
    @DisplayName("Test listing features with UUIDs")
    void list() {
        assertThrows(NotImplementedException.class, () -> featureService
                .list(List.of(UUID.randomUUID().toString(), UUID.randomUUID().toString()), true));
    }

    @Test
    @DisplayName("Test updating a feature")
    void update() {
        assertThrows(NotImplementedException.class, () -> featureService.update(UUID.randomUUID(), new Feature()));
    }

    @Test
    @DisplayName("Test deleting a feature")
    void delete() {
        assertThrows(NotImplementedException.class, () -> featureService.delete(UUID.randomUUID(), new Feature()));
    }

    @Test
    @DisplayName("Test listing features with IDs")
    void testList() {
        assertThrows(NotImplementedException.class, () -> featureService.list(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()));
    }
}
