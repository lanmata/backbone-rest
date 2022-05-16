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

import com.prx.commons.pojo.Feature;
import com.prx.persistence.general.domains.FeatureEntity;
import org.apache.commons.lang.NotImplementedException;

/**
 * FeatureTestTemplate.
 *
 * @author Luis Antonio Mata
 * @version 1.0.0, 14-05-2022
 * @since 11
 */
public enum FeatureTemplateTest implements TemplateUtil<Feature, FeatureEntity>{
    FEATURE_TO {
        @Override
        public Feature getModel() {
            final var feature = new Feature();
            feature.setId(1L);
            feature.setActive(true);
            feature.setName("Nombre de feature");
            feature.setDescription("Descripcin de feature");
            return feature;
        }

        @Override
        public FeatureEntity getEntity() {
            throw new NotImplementedException();
        }
    }
}
