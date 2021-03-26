/*
 * @(#)FeatureMessageKey.java.
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

package com.prx.backoffice.enums.keys;

import com.prx.commons.enums.types.MessageType;

/**
 * FeatureMessageKey.
 *
 * @author <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 14-02-2021
 */
public enum FeatureMessageKey  implements MessageType {
    FEATURE_ERROR_CREATED(1,"Error durante la creación de la caracteristica"),
    FEATURE_OK(200,"Ok"),
    FEATURE_CREATED(201,"Caracteristica creada"),
    FEATURE_NOT_FOUND(404,"Caracteristica no encontrado"),
    FEATURE_CREATE_ERROR(404,"Error durante la creación de la caracteristica"),
    FEATURE_PREVIOUS_EXIST(409,"Nombre de la caracteristica ya se encuentra ocupada, ingrese un nombre de caracteristica diferente");

    private final int code;
    private final String status;

    FeatureMessageKey(int code, String status){
        this.code = code;
        this.status = status;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getStatus() {
        return status;
    }
}
