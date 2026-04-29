/*
 *  @(#)RoleCollectionResponseTest.java
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

package com.umdc.backoffice.v1.roles.api.to;

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
 * RolCollectionResponseTest.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 18-02-2021
 */
public class RoleCollectionResponseTest {

    @Test
    @DisplayName("Test getters and setters of RoleCollectionResponse")
    public void testGettersAndSetters() {
        final var uuid = UUID.randomUUID();
        final var rolCollectionResponse = new RoleCollectionResponse();
        final var rol = new Role();
        final var feature = new Feature();
        feature.setId(uuid);
        feature.setActive(true);
        feature.setName("Feature name");
        feature.setDescription("Feature description");
        rol.setId(uuid);
        rol.setActive(true);
        rol.setName("Nombre de rol");
        rol.setFeatures(new ArrayList<>());
        rol.getFeatures().add(feature);
        rol.setDescription("Role description");
        rolCollectionResponse.setRoles(new ArrayList<>());
        rolCollectionResponse.getRoles().add(rol);
        rolCollectionResponse.setMessage("OK");
        rolCollectionResponse.setCode(200);
        rolCollectionResponse.setDateTime(LocalDateTime.now(ZoneId.systemDefault()));

        assertAll(
                () -> assertNotNull(rolCollectionResponse),
                () -> assertNotNull(rolCollectionResponse.toString()),
                () -> assertNotNull(rolCollectionResponse.getRoles()),
                () -> assertNotNull(rolCollectionResponse.getRoles().toString()),
                () -> assertNotNull(rolCollectionResponse.getCode()),
                () -> assertNotNull(rolCollectionResponse.getMessage()),
                () -> assertNotNull(rolCollectionResponse.getDateTime()),
                () -> assertNotNull(rolCollectionResponse.getRoles().toString())
        );

        rolCollectionResponse.getRoles().forEach(rol1 -> {
            assertNotNull(rol1.getId());
            assertNotNull(rol1.getName());
            assertNotNull(rol1.toString());
            assertNotNull(rol1.getActive());
            assertNotNull(rol1.getDescription());
        });
        rolCollectionResponse.getRoles().forEach(rol1 ->
            rol1.getFeatures().forEach(feature1 -> {
                assertNotNull(feature1.getId());
                assertNotNull(feature1.getName());
                assertNotNull(feature1.toString());
                assertNotNull(feature1.getActive());
                assertNotNull(feature1.getDescription());
            })
        );
    }
}
