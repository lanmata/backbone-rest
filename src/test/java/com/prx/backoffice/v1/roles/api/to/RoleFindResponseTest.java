/*
 *  @(#)RoleFindResponseTest.java
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

package com.prx.backoffice.v1.roles.api.to;

import com.prx.commons.general.pojo.Feature;
import com.prx.commons.general.pojo.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * RolFindResponseTest.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 18-02-2021
 */
public class RoleFindResponseTest {

    @Test
    @DisplayName("Test getters and setters of RoleFindResponse")
    public void testGettersAndSetters(){
        final var uuid = UUID.randomUUID();
        final var roleFindResponse = new RoleFindResponse();
        final var role = new Role();
        final var feature = new Feature();
        feature.setId(uuid);
        feature.setActive(true);
        feature.setName("Feature name");
        feature.setDescription("Feature description");
        role.setId(uuid);
        role.setActive(true);
        role.setName("Role name");
        role.setFeatures(new ArrayList<>());
        role.getFeatures().add(feature);
        role.setDescription("Role description");
        roleFindResponse.setRol(role);
        roleFindResponse.setMessage("OK");
        roleFindResponse.setCode(200);
        roleFindResponse.setDateTime(LocalDateTime.now(ZoneId.systemDefault()));

        assertAll(
                () -> assertNotNull(roleFindResponse),
                () -> assertNotNull(roleFindResponse.toString()),
                () -> assertNotNull(roleFindResponse.getRol()),
                () -> assertNotNull(roleFindResponse.getCode()),
                () -> assertNotNull(roleFindResponse.getMessage()),
                () -> assertNotNull(roleFindResponse.getDateTime()),
                () -> assertNotNull(roleFindResponse.getRol().getId()),
                () -> assertNotNull(roleFindResponse.getRol().getName()),
                () -> assertNotNull(roleFindResponse.getRol().getActive()),
                () -> assertNotNull(roleFindResponse.getRol().getDescription()),
                () -> assertNotNull(roleFindResponse.getRol().getFeatures()),
                () -> assertNotNull(roleFindResponse.getRol().toString()),
                () -> assertNotNull(roleFindResponse.toString())
        );
        roleFindResponse.getRol().getFeatures().forEach(feature1 -> {
            assertNotNull(feature1.getId());
            assertNotNull(feature1.getName());
            assertNotNull(feature1.toString());
            assertNotNull(feature1.getActive());
            assertNotNull(feature1.getDescription());
        });
    }
}
