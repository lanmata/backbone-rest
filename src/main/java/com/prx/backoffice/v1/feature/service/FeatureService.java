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

package com.prx.backoffice.v1.feature.service;

import com.prx.backoffice.services.CrudService;
import com.prx.commons.pojo.Feature;
import com.prx.commons.pojo.MessageActivity;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * FeatureService.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 14-02-2021
 */
public interface FeatureService extends CrudService <Feature> {

    /**
     * Busca uno o más {@link Feature} en base a uno o más id´s.
     *
     * @param featureIds {@link List}
     * @param includeInactive {@link boolean}
     * @return Objeto de tipo {@link MessageActivity}
     */
    ResponseEntity<List<Feature>> list(List<Long> featureIds, boolean includeInactive);

}
