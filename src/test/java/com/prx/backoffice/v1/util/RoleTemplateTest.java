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

package com.prx.backoffice.v1.util;

import com.prx.commons.pojo.Role;
import com.prx.persistence.general.domains.RoleEntity;
import org.apache.commons.lang.NotImplementedException;

import java.util.ArrayList;

/**
 * RoleTemplateTest.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 14-05-2022
 * @since 11
 */
public enum RoleTemplateTest implements TemplateUtil<Role, RoleEntity> {
    ROLE_TO {
        @Override
        public Role getModel() {
            final var role = new Role();
            role.setId(11L);
            role.setActive(true);
            role.setName("Nombre de rol");
            role.setDescription("Descripcion de rol");
            role.setFeatures(new ArrayList<>());
            role.getFeatures().add(FeatureTemplateTest.FEATURE_TO.getModel());
            return role;
        }

        @Override
        public RoleEntity getEntity() {
            throw new NotImplementedException();
        }
    }
}
