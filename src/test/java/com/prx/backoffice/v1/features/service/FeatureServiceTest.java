package com.prx.backoffice.v1.features.service;

import com.prx.commons.pojo.Feature;
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
        assertThrows(NotImplementedException.class, () -> featureService.update("abc", new Feature()));
    }

    @Test
    @DisplayName("Test deleting a feature")
    void delete() {
        assertThrows(NotImplementedException.class, () -> featureService.delete("abc", new Feature()));
    }

    @Test
    @DisplayName("Test listing features with IDs")
    void testList() {
        assertThrows(NotImplementedException.class, () -> featureService.list("abc1", "abc2", "abc3"));
    }
}
