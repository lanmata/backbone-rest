/*
 *  @(#)FeatureMessageKey.java
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

package com.prx.backoffice.constant.keys;


import com.prx.commons.constants.types.MessageType;

/**
 * Enum representing various feature message keys used in the application.
 * Implements the MessageType interface.
 * Provides message codes and status descriptions for different feature-related operations.
 *
 * @author
 * <a href='mailto:luis.antonio.mata@gmail.com'>Luis Antonio Mata</a>
 * @version 1.0.0, 14-02-2021
 */
public enum FeatureMessageKey implements MessageType {
    /**
     * Error during feature creation.
     */
    FEATURE_ERROR_CREATED(1, "Error durante la creación de la caracteristica"),

    /**
     * Operation successful.
     */
    FEATURE_OK(200, "Ok"),

    /**
     * Feature created successfully.
     */
    FEATURE_CREATED(201, "Caracteristica creada"),

    /**
     * Feature not found.
     */
    FEATURE_NOT_FOUND(404, "Caracteristica no encontrado"),

    /**
     * Error during feature creation.
     */
    FEATURE_CREATE_ERROR(404, "Error durante la creación de la caracteristica"),

    /**
     * Feature name already exists.
     */
    FEATURE_PREVIOUS_EXIST(409, "Nombre de la caracteristica ya se encuentra ocupada, ingrese un nombre de caracteristica diferente");

    private final int code;
    private final String status;

    /**
     * Constructor for FeatureMessageKey enum.
     *
     * @param code the code of the message
     * @param status the status description of the message
     */
    FeatureMessageKey(int code, String status) {
        this.code = code;
        this.status = status;
    }

    /**
     * Gets the code of the message.
     *
     * @return the code of the message
     */
    @Override
    public int getCode() {
        return code;
    }

    /**
     * Gets the status description of the message.
     *
     * @return the status description of the message
     */
    @Override
    public String getStatus() {
        return status;
    }
}
